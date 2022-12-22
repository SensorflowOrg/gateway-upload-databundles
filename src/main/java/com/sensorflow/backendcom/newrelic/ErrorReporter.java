package com.sensorflow.backendcom.newrelic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ErrorReporter {

    private ErrorReporter(){}

    @SafeVarargs
    public static void reportError(String message, ErrorTypes errorType, Map.Entry<String,Object>... parameters ){
        log.error("{} {} {}", errorType, message, Arrays.toString(parameters));
        Map<String, Object> params = new HashMap<>();
        for (Map.Entry<String, Object> parameter : parameters) {
            params.put(parameter.getKey(),parameter.getValue());
        }
        String caller = Thread.currentThread().getStackTrace()[2].getMethodName()
                + ":" + Thread.currentThread().getStackTrace()[2].getLineNumber();
        params.put("error.message",message);
        params.put("error.class",errorType.toString());
        params.put("error.thrownAt", caller);
        EventReporter.recordEvent("TransactionError",params);
    }

    @SafeVarargs
    public static void reportError(Throwable e, Map.Entry<String,Object>... parameters ){
        log.error("{},{}",parameters,e);
        Map<String, Object> params = new HashMap<>();
        for (Map.Entry<String, Object> parameter : parameters) {
            params.put(parameter.getKey(),parameter.getValue());
        }
        params.put("error.class",e.getClass().toString());
        params.put("error.message", e.getMessage());
        if(e.getStackTrace().length>0){
            params.put("error.thrownAt", e.getStackTrace()[0].toString());
        }
        EventReporter.recordEvent("TransactionError", e, params);
    }
}
