package com.demo.testcase.webapi.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class AmqpRabbitConfiguration {
    @Autowired
    private ApplicationProperties applicationProperties;

    @Bean
	DirectExchange deadLetterExchange() {
		return new DirectExchange(applicationProperties.getDeadLetterExchange());
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(applicationProperties.getDeadLetterQueue()).build();
    }

    @Bean
	Binding bindingDeadLetter(DirectExchange deadLetterExchange, Queue deadLetterQueue) {
		return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(applicationProperties.getDeadLetterKey());
	}

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(applicationProperties.getExchange());
    }

    @Bean
    public Queue queueRequest() {
        return QueueBuilder.durable(applicationProperties.getQueueRequest()).withArgument("x-dead-letter-exchange", applicationProperties.getDeadLetterExchange())
        .withArgument("x-dead-letter-routing-key", applicationProperties.getDeadLetterKey()).build();
    }

    @Bean
    public Queue queueResponse() {
        return QueueBuilder.durable(applicationProperties.getQueueResponse()).withArgument("x-dead-letter-exchange", applicationProperties.getDeadLetterExchange())
        .withArgument("x-dead-letter-routing-key", applicationProperties.getDeadLetterKey()).build();    }

    @Bean
    public AsyncRabbitTemplate asyncRabbitTemplate(RabbitTemplate rabbitTemplate){
        return new AsyncRabbitTemplate(rabbitTemplate);
    }

    @Bean
    public Binding bindingRequest(DirectExchange exchange, Queue queueRequest) {
        return BindingBuilder.bind(queueRequest)
            .to(exchange)
            .with(applicationProperties.getKeyRequest());
    }

    @Bean
    public Binding bindingResponse(DirectExchange exchange, Queue queueResponse) {
        return BindingBuilder.bind(queueResponse)
            .to(exchange)
            .with(applicationProperties.getKeyResponse());
    }

    @Bean
    public MessageConverter jackson2MessageConverter() {
      return new Jackson2JsonMessageConverter();
    }
}