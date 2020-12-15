package com.demo.testcase.calculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import lombok.NonNull;

import org.springframework.amqp.core.Message;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.concurrent.Executor;

import com.demo.testcase.utils.MathOperation;
import com.demo.testcase.utils.Result;

@Service
public class CalculatorWorker {
    @Autowired
    private AmqpTemplate rabbitTemplate;
    @Autowired
    private Executor executor;
    @Value("${demo.calculatorworker.precision:5}")
    private int precision;

    final Logger logger = LoggerFactory.getLogger(CalculatorWorker.class);

    @RabbitListener(queues = "${applicationproperties.queuerequest}")
    public void receiveMessage(MathOperation operation, Message msg) {

        if (msg == null || operation == null) {
            logger.error("receive | request | invalid data");
            return;
        }

        String correlationId = msg.getMessageProperties().getCorrelationId();
        if (correlationId == null || correlationId.isEmpty()) {
            logger.error("receive | request | invalid correlationId");
            return;
        }

        MDC.put("correlation_id", correlationId);
        logger.info("MathOperation Received | operation=" + operation);

        ListenableFuture<Result> resultFuture = processMathOperation(operation);

        resultFuture.addCallback(result -> {
            logger.info("MathOperation Done | send result | result=" + result);

            rabbitTemplate.convertAndSend(msg.getMessageProperties().getReceivedExchange(),
                    msg.getMessageProperties().getReplyTo(), result, message -> {
                        message.getMessageProperties().setCorrelationId(msg.getMessageProperties().getCorrelationId());
                        return message;
                    });

            MDC.clear();
        }, t -> {
            logger.error("MathOperation: operatio error ", t);

            rabbitTemplate.convertAndSend(msg.getMessageProperties().getReceivedExchange(),
                    msg.getMessageProperties().getReplyTo(),
                    new Result(null, msg.getMessageProperties().getCorrelationId(), "error performing math operation"),
                    message -> {
                        message.getMessageProperties().setCorrelationId(msg.getMessageProperties().getCorrelationId());
                        return message;
                    });

            MDC.clear();
        });

    }

    private ListenableFuture<Result> processMathOperation(@NonNull MathOperation mathOperation) {

        SettableListenableFuture<Result> future = new SettableListenableFuture<Result>();
        executor.execute(() -> {
            logger.info("exec operation | mathOperation=" + mathOperation);

            if (mathOperation.getOperationType() == MathOperation.OperationType.Add) {
                future.set(new Result(mathOperation.getA().add(mathOperation.getB(), new MathContext(precision)), mathOperation.getCorrelationId(),
                        null));
                return;
            }

            if (mathOperation.getOperationType() == MathOperation.OperationType.Subtraction) {

                future.set(new Result(mathOperation.getA().subtract(mathOperation.getB(), new MathContext(precision)),
                        mathOperation.getCorrelationId(), null));
                return;
            }

            if (mathOperation.getOperationType() == MathOperation.OperationType.Division) {
                if (mathOperation.getB().equals(BigDecimal.ZERO)) {
                    future.set(new Result(null, mathOperation.getCorrelationId(), "error division by 0 not allowed"));
                    return;
                }

                future.set(new Result(mathOperation.getA().divide(mathOperation.getB(), new MathContext(precision)),
                        mathOperation.getCorrelationId(), null));
                return;
            }

            if (mathOperation.getOperationType() == MathOperation.OperationType.Multiplication) {
                future.set(new Result(mathOperation.getA().multiply(mathOperation.getB(), new MathContext(precision)),
                        mathOperation.getCorrelationId(), null));
                return;
            }
        });

        return future;
    }

}