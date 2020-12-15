package com.demo.testcase.webapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import lombok.Getter;

@ConstructorBinding
@ConfigurationProperties(prefix = "applicationproperties")
@Getter
public class ApplicationProperties {

    private final String exchange;

    private final String queueRequest;
    private final String queueResponse;

    private final String keyRequest;
    private final String keyResponse;

    private final String deadLetterExchange;
    private final String deadLetterQueue;
    private final String deadLetterKey;

    public ApplicationProperties(final String exchange, final String queueRequest, final String queueResponse,
            String keyRequest, String keyResponse, String deadLetterExchange, String deadLetterQueue,
            String deadLetterKey) {
        this.exchange = exchange;
        this.queueRequest = queueRequest;
        this.queueResponse = queueResponse;
        this.keyRequest = keyRequest;
        this.keyResponse = keyResponse;
        this.deadLetterExchange = deadLetterExchange;
        this.deadLetterQueue = deadLetterQueue;
        this.deadLetterKey = deadLetterKey;
    }

    // https://stackoverflow.com/questions/32386177/rabbitlistener-defining-queues-from-properties
}