package com.valor.mercury.gateway.aop;


import com.mfc.config.ConfigTools3;
import com.valor.mercury.gateway.util.HttpUtils;
import com.valor.mercury.gateway.model.dto.QueryResponseDTO;
import com.valor.mercury.gateway.util.JsonUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Component
@Aspect
public class ControllerLogAspect {

    private final Logger logger = LoggerFactory.getLogger(ControllerLogAspect.class);

    @Pointcut("@annotation(com.valor.mercury.gateway.aop.GateWayApiCall)")
    public void GateWayApiCall() {}

    @Around("GateWayApiCall()")
    public Object printLog(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        if (ConfigTools3.getAsBoolean("loggable.controller", true)) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            logger.info("[REQUEST] [HOST]:[{}], [API]:[{}], [METHOD]:[{}], [ARGS]:[{}]", HttpUtils.getIpAddress(request), request.getRequestURL(), request.getMethod(), JsonUtils.toJsonString(request.getParameterMap()));
            long startTime = System.currentTimeMillis();
            result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            logger.info("[RESPONSE] [BODY]:[{}], [TIME_MILLIS]:[{}] ms",  result.toString(), endTime - startTime);
        } else {
            result = joinPoint.proceed();
        }
        return result;
    }
}
