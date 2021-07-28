package com.valor.mercury.receiver.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapUtils {

    public static ConcurrentHashMap<String, Object> toConcurrentHashMap(Map<String, Object> map) {
        ConcurrentHashMap<String, Object> resultMap = new ConcurrentHashMap<>();
        for(Map.Entry<String, Object> entry : map.entrySet()) {
            //Map.Entry<String, Object> entry = (Map.Entry) obj;
            resultMap.put(entry.getKey(), entry.getValue());
        }
        return resultMap;
    }

    public static HashMap<String, Object> revertConcurrentHashMap(Map<String, Object> map) {
        HashMap<String, Object> resultMap = new HashMap<>();
        for(Map.Entry<String, Object> entry : map.entrySet()) {
            //Map.Entry entry = (Map.Entry) obj;
            resultMap.put(entry.getKey(), entry.getValue());
        }
        return resultMap;
    }
}
