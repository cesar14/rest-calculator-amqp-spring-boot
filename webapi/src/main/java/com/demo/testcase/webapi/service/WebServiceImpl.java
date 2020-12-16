package com.demo.testcase.webapi.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import com.demo.testcase.utils.GlobalDefinitions;
import com.demo.testcase.utils.MathOperation;
import com.demo.testcase.utils.Result;
import com.demo.testcase.webapi.config.ApplicationProperties;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import lombok.NonNull;

@Service
public class WebServiceImpl implements WebService {
    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Autowired
    private Executor executor;

    @Autowired
    private ApplicationProperties applicationProperties;

    private Map<String, MathResult> mRequests = new HashMap<>();

    Logger logger = LoggerFactory.getLogger(WebServiceImpl.class);

    @Override
    public void mathOp(@NonNull MathOperation mathOperation, @NonNull MathResult mathCb) {
        executor.execute(() -> {
            logger.info("MathOperation operation=" + mathOperation);

            MessagePostProcessor messagePostProcessor = message -> {
                MessageProperties messageProperties = message.getMessageProperties();
                messageProperties.setReplyTo(applicationProperties.getKeyResponse());
                messageProperties.setCorrelationId(mathOperation.getCorrelationId());

                return message;
            };

            synchronized (mRequests) {
                mRequests.put(mathOperation.getCorrelationId(), mathCb);
            }

            rabbitTemplate.convertAndSend(applicationProperties.getExchange(), applicationProperties.getKeyRequest(), mathOperation, messagePostProcessor);
            logger.info("MathOperation request made");
        });
    }

    @RabbitListener(queues = "${applicationproperties.queueresponse}")
    public void onReceive(@Payload Result result, @Header(AmqpHeaders.CORRELATION_ID) String correlationId) {
        if (correlationId == null || result == null || correlationId.isEmpty()) {
            logger.error("onReceive | result | invalid data | correlationId=" + correlationId + " | result=" + result);
            return;
        }

        MDC.put(GlobalDefinitions.CorrelationIdKey, correlationId);
        logger.info("onReceive | math result response | result=" + result);

        if (result.getCorrelationId() != null && !correlationId.equals(result.getCorrelationId())) {
            logger.warn("onReceive | math result correlationId mismatch | hCorrelationId=" + correlationId, " | rCorrelationId=" + result.getCorrelationId());
        }

       processMathResult(result);
    }

    @RabbitListener(queues = "${applicationproperties.deadletterqueue}")
    public void onFailure(@Header(AmqpHeaders.CORRELATION_ID) String correlationId) {
        if (correlationId == null || correlationId.isEmpty()) {
            logger.error("onFailure | response | invalid data | correlationId=" + correlationId);
            return;
        }

        MDC.put(GlobalDefinitions.CorrelationIdKey, correlationId);
        logger.error("onFailure | math result response | result=" + correlationId);

        processMathResult(new Result(null, correlationId, "error processing math operation"));
    }


    private void processMathResult(@NonNull Result result) {

        executor.execute(() -> {
            MathResult mathResult = null;
            synchronized (mRequests) {
                mathResult = mRequests.remove(result.getCorrelationId());
            }

            if (mathResult == null) {
                logger.warn("receive | response | no math operation callback found");
                return;
            }

            mathResult.onCompleted(result);
        });
    }
}