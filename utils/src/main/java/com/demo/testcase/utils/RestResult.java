package com.demo.testcase.utils;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.NonNull;
import lombok.Value;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Value
public class RestResult {
    private String error;
    private BigDecimal result;

    public RestResult(@NonNull Result result) {
        if (result.isValid()) {
            this.error = result.getError();
            this.result = null;
            return;
        }

        this.error = null;
        this.result = result.getResult();
    }
}