package com.valor.mercury.manager.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class MyExceptionHandler {
    private Logger logger = LoggerFactory.getLogger("MyExceptionHandler");

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Map<String, Object> errorHandler(Exception ex) {
        Map<String, Object> map = new HashMap<>();
        if (ex instanceof IException) {
            map.put("code", ((IException) ex).getCode());
            map.put("msg", ex.getMessage());
        } else {
            String message = ex.getMessage();
            map.put("code", 500);
            map.put("msg", message == null || message.trim().isEmpty() ? "系统繁忙" : message);
            logger.error(ex.getMessage(), ex);
            ex.printStackTrace();
        }
        return map;
    }

}
