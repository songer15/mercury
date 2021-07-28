package com.valor.mercury.receiver.aop;


import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableControllerLog {

    String description() default  "";
}
