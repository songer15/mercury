package com.valor.mercury.receiver.aop;


import com.valor.mercury.common.model.dto.ResponseEntity;
import com.valor.mercury.common.util.JsonUtils;
import com.valor.mercury.receiver.util.HttpUtils;
import com.valor.mercury.receiver.config.CommonConfig;
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

    @Pointcut("@annotation(com.valor.mercury.receiver.aop.EnableControllerLog)")
    public void controllerLog() {
    }

    @Around("controllerLog()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        if (CommonConfig.isControllerLoggable()) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            logger.info("[REQUEST] [HOST]:[{}], [API]:[{}], [METHOD]:[{}], [ARGS]:[{}]", HttpUtils.getIpAddress(request), request.getRequestURL(), request.getMethod(), JsonUtils.toJsonString(request.getParameterMap()));
            long startTime = System.currentTimeMillis();
            result = joinPoint.proceed();
            ResponseEntity<?> responseEntity = (ResponseEntity) result;
            logger.info("[RESPONSE] [BODY]:[{}], [TIME_MILLIS]:[{}],  ms", responseEntity, System.currentTimeMillis() - startTime);
        } else {
            result = joinPoint.proceed();
        }
        return result;
    }
}
