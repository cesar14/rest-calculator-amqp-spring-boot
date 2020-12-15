package com.demo.testcase.webapi.service;

import com.demo.testcase.utils.MathOperation;
import com.demo.testcase.utils.Result;

public interface WebService {
      @FunctionalInterface
      interface MathResult {
            void onCompleted(Result result);
      }

      public abstract void mathOp(MathOperation mathOperation, MathResult mathCb);
}