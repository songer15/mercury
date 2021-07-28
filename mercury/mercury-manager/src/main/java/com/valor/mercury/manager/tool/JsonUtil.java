package com.valor.mercury.manager.tool;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Author: Gavin
 * @Date: 2020/2/20 8:39
 */
public class JsonUtil {

    public static ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, Object> objectToMap(Object object) {
        Map<String, Object> result = new HashMap<>();
        Field[] fields = object.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String name = new String(field.getName());
                result.put(name, field.get(object));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        List<T> result = null;
        try {
            result = JSON.parseArray(json, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result == null) {
            result = new ArrayList<>();
        }
        return result;
    }


    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }
        T obj = null;
        try {
            obj = clazz.newInstance();
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getName().startsWith("set")) {
                    String field = method.getName();
                    field = field.substring(field.indexOf("set") + 3);
                    field = field.toLowerCase().charAt(0) + field.substring(1);
                    if (map.containsKey(field)) {
                        method.invoke(obj, map.get(field));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static Map<String, Object> jsonToMap(String json) {
        return (Map)JSON.parseObject(json, Map.class);
    }

}
