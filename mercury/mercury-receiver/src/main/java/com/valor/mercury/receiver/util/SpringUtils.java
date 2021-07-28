package com.valor.mercury.receiver.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理spring bean
 */
@Component
public class SpringUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringUtils.applicationContext == null)
            SpringUtils.applicationContext = applicationContext;
    }

    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    /**
     * 获得T类型所有子类或实现类的bean的集合
     */
    public static <T> Map<String, T> getBeans(Class<T> klass) {
        Map<String, T> map = new HashMap<>();
        Arrays.stream(applicationContext.getBeanDefinitionNames())
                 .filter(beanName ->  klass.isAssignableFrom(applicationContext.getBean(beanName).getClass()))
                 .forEach(beanName ->  map.put(beanName, (T)applicationContext.getBean(beanName)));
        return map;
    }
}
