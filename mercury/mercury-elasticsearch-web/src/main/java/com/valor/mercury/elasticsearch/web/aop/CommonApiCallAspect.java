package com.valor.mercury.elasticsearch.web.aop;


import com.valor.mercury.elasticsearch.web.model.APIException;
import com.valor.mercury.elasticsearch.web.model.JsonResult;
import com.valor.mercury.elasticsearch.web.model.PageResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Aspect
@Component
public class CommonApiCallAspect {

    private final Logger logger = LoggerFactory.getLogger(CommonApiCallAspect.class);

    @Pointcut("@annotation(CommonApiCall)")
    public void excute() {
    }

    @Around("excute()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        HttpServletRequest request = (HttpServletRequest) joinPoint.getArgs()[0];
        HttpServletResponse response = (HttpServletResponse) joinPoint.getArgs()[1];

        String apiName = request.getRequestURI();
        String ip = HttpTools.getRemoteHost(request);
        Object[] args = joinPoint.getArgs();

        String returnTypeName = ((MethodSignature)joinPoint.getSignature()).getMethod().getGenericReturnType().getTypeName();

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Object result = null;
        long duration = -1L;
        try {
            result = joinPoint.proceed(args);
            duration = System.currentTimeMillis() - startTime;
            if (result instanceof JsonResult) {
                logger.info("Got [{}] request from[{}][{}],args:[{}], result:[{}],timeMs:[{}] ms", apiName, user.getUsername(), ip, args, ((JsonResult) result).get("code"), duration);
            } else
                logger.info("Got [{}] request from [{}][{}],args:[{}], timeMs:[{}] ms", apiName, user.getUsername(), ip, args, duration);
            return result;
        } catch (Exception e) {
            logger.error("Got [{}] request error from [{}],args:[{}].", apiName, ip, args, e);
            if (e instanceof APIException) {
                APIException apiException = (APIException) e;
                if (returnTypeName.equals(PageResult.class.getTypeName())) {
                    return PageResult.error(apiException.getRetCode(), apiException.getErrCode(), apiException.getMsg());
                } else if (returnTypeName.equals(JsonResult.class.getTypeName())) {
                    return JsonResult.error(apiException.getRetCode(), apiException.getErrCode(), apiException.getMsg());
                }else
                    throw e;
            }else
                throw e;
        }

    }

}
