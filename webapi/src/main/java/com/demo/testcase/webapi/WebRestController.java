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
      return processRequest(a, b, MathOperation.OperationType.Add, response);
   }

   @GetMapping("/subtract")
   public DeferredResult<RestResult> subtract(@RequestParam BigDecimal a, @RequestParam BigDecimal b, HttpServletResponse response) {
      return processRequest(a, b, MathOperation.OperationType.Subtraction, response);
   }

   @GetMapping("/multiply")
   public DeferredResult<RestResult> multiply(@RequestParam BigDecimal a, @RequestParam BigDecimal b, HttpServletResponse response) {
      return processRequest(a, b, MathOperation.OperationType.Multiplication, response);
   }

   @GetMapping("/division")
   public DeferredResult<RestResult> divide(@RequestParam BigDecimal a, @RequestParam BigDecimal b, HttpServletResponse response) {
      return processRequest(a, b, MathOperation.OperationType.Division, response);
   }

   DeferredResult<RestResult> processRequest(BigDecimal a, BigDecimal b, MathOperation.OperationType operationType, HttpServletResponse response) {
      @NonNull
      String correlationId = UUID.randomUUID().toString();
      MDC.put(GlobalDefinitions.CorrelationIdKey, correlationId);

      logger.info("division request | a=" + a + " b=" + b);

      if (response != null)
         response.setHeader(GlobalDefinitions.XCorrelationIdHeader, correlationId);

      DeferredResult<RestResult> deferredResult = new DeferredResult<>(timeout);
      if (a == null || b == null) {
         logger.error("request | invalid request  | a=" + a + " b=" + b);
         deferredResult.setErrorResult(new RestResult("error invalid arguments"));
         return deferredResult;
      }

      webService.mathOp(new MathOperation(a, b, correlationId, operationType), result -> {
         deferredResult.setErrorResult(new RestResult(result));
         MDC.clear();
      });

      return deferredResult;
   }
}