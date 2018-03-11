package com.github.since1986.demo.gateway.controller;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by since1986 on 2017/4/26.
 */

//全局异常处理
@RestControllerAdvice
public class AppExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus
    public Map<String, Object> handleException(Exception exception) {
        exception.printStackTrace();
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("message", exception.getLocalizedMessage());
        response.put("code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return response;
    }
}


