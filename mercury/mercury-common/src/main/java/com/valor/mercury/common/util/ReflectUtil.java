package com.valor.mercury.common.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用反射实现的一些工具方法
 */
public class ReflectUtil {

    /**
     * 把对象转成Map，只包含指定字段
     */
    public static <T> Map<String, Object> objectToMap(T t) {
        if (t == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        Field[] fields = t.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(t));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

}
