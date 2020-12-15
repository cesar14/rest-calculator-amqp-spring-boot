package com.demo.testcase.utils;

import java.math.BigDecimal;

import lombok.Value;

@Value
public class MathOperation {
    public enum OperationType {
        None, Add, Subtraction, Multiplication, Division;
    }

    private BigDecimal a;
    private BigDecimal b;
    private String correlationId;
    private OperationType operationType;
}