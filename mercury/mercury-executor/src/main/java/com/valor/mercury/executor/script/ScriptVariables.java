package com.valor.mercury.executor.script;

import java.util.Map;

public class ScriptVariables {

    private static final ThreadLocal<Map<String, Object>> PARAMS = new ThreadLocal<>();

    public <T> T get(String key) {
        Map<String, Object> map = PARAMS.get();
        if (map != null) {
            return (T) map.get(key);
        }
        return null;
    }

    public void setParams(Map<String, Object> params) {
        PARAMS.set(params);
    }

    public Map<String, Object> getParams() {
        return PARAMS.get();
    }

    public void clear() {
        PARAMS.set(null);
    }
}
