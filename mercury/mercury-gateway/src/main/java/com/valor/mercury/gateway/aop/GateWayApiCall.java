package com.valor.mercury.gateway.aop;


import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GateWayApiCall {

    String description() default  "";
    boolean isEnable() default true;
}
