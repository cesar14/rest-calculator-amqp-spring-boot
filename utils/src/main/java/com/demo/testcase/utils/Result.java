package com.demo.testcase.utils;

import java.math.BigDecimal;

import lombok.Value;

@Value
public class Result {
    private BigDecimal result;
    private String correlationId;
    private String error;

    Boolean hasError() {
        return error != null && !error.isEmpty();
    }
}