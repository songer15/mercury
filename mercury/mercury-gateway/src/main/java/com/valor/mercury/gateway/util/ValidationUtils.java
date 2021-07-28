package com.valor.mercury.gateway.util;

import org.hibernate.validator.HibernateValidator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

public class ValidationUtils {

    private static boolean isValidatorInit = false;

    private static Validator validator;

    /**
     *
     */
    public synchronized static <T> void validate(T obj) {
        if (!isValidatorInit) {
            validator = Validation.byProvider(HibernateValidator.class).configure().failFast(true).buildValidatorFactory().getValidator();
            isValidatorInit = true;
        }
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(obj);
        // 抛出检验异常
        if (constraintViolations.size() > 0) {
            throw new RuntimeException(String.format("参数校验失败:%s", constraintViolations.iterator().next().getMessage()));
        }
    }
}