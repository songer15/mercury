package com.valor.mercury.executor.script.api;

import java.util.Map;

public interface CommonScriptInterceptor {

    void preHandle(String scriptName, Object scriptInstance, Map<String, Object> scriptInvocationParams) throws Exception;

}
