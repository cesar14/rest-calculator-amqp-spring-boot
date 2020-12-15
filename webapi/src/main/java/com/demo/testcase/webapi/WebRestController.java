package com.demo.testcase.webapi;

import java.math.BigDecimal;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import lombok.NonNull;

import org.springframework.web.context.request.async.DeferredResult;

import com.demo.testcase.utils.*;
import com.demo.testcase.webapi.service.WebService;

@RestController
public class WebRestController {

   @Autowired
   private WebService webService;

   Logger logger = LoggerFactory.getLogger(WebRestController.class);

   @Value("${demo.webrestCcontroller.requesttimeout:10000}")
   private long timeout;

   @GetMapping("/add")
   public DeferredResult<RestResult> add(@RequestParam BigDecimal a, @RequestParam BigDecimal b, HttpServletResponse response) {
      @NonNull
      String correlationId = UUID.randomUUID().toString();
      MDC.put("correlation_id", correlationId);

      logger.info("add request | a=" + a + " b=" + b);

      response.setHeader("X-Correlation-ID", correlationId);
      DeferredResult<RestResult> deferredResult = new DeferredResult<>(timeout);
      webService.mathOp(new MathOperation(a, b, correlationId, MathOperation.OperationType.Add), result -> {
         deferredResult.setResult(new RestResult(result));

         MDC.clear();
      });

      return deferredResult;
   }

   @GetMapping("/subtract")
   public DeferredResult<RestResult> subtract(@RequestParam BigDecimal a, @RequestParam BigDecimal b, HttpServletResponse response) {
      @NonNull
      String correlationId = UUID.randomUUID().toString();
      MDC.put("correlation_id", correlationId);

      logger.info("subtract request | a=" + a + " b=" + b);

      response.setHeader("X-Correlation-ID", correlationId);
      DeferredResult<RestResult> deferredResult = new DeferredResult<>(timeout);
      webService.mathOp(new MathOperation(a, b, correlationId, MathOperation.OperationType.Subtraction), result -> {
         deferredResult.setResult(new RestResult(result));
         MDC.clear();
      });

      return deferredResult;
   }

   @GetMapping("/multiply")
   public DeferredResult<RestResult> multiply(@RequestParam BigDecimal a, @RequestParam BigDecimal b, HttpServletResponse response) {
      @NonNull
      String correlationId = UUID.randomUUID().toString();
      MDC.put("correlation_id", correlationId);

      logger.info("multiply request | a=" + a + " b=" + b);

      response.setHeader("X-Correlation-ID", correlationId);
      DeferredResult<RestResult> deferredResult = new DeferredResult<>(timeout);
      webService.mathOp(new MathOperation(a, b, correlationId, MathOperation.OperationType.Multiplication), result -> {
         deferredResult.setResult(new RestResult(result));
         MDC.clear();
      });

      return deferredResult;
   }

   @GetMapping("/division")
   public DeferredResult<RestResult> divide(@RequestParam BigDecimal a, @RequestParam BigDecimal b, HttpServletResponse response) {
      @NonNull
      String correlationId = UUID.randomUUID().toString();
      MDC.put("correlation_id", correlationId);

      logger.info("division request | a=" + a + " b=" + b);

      response.setHeader("X-Correlation-ID", correlationId);
      DeferredResult<RestResult> deferredResult = new DeferredResult<>(timeout);
      webService.mathOp(new MathOperation(a, b, correlationId, MathOperation.OperationType.Division), result -> {
         deferredResult.setErrorResult(new RestResult(result));
         MDC.clear();
      });

      return deferredResult;
   }
}