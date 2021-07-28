package com.valor.mercury.executor.script.groovy;

import com.valor.mercury.executor.script.ScriptVariables;
import com.valor.mercury.executor.script.api.CommonScriptInterceptor;
import com.valor.mercury.executor.script.ScriptManager;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovySystem;
import groovy.lang.Script;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.kohsuke.groovy.sandbox.GroovyInterceptor;
import org.kohsuke.groovy.sandbox.SandboxTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroovyScriptManager implements ScriptManager<Script> {

    protected static final Logger logger = LoggerFactory.getLogger(GroovyScriptManager.class);

    private final ConcurrentHashMap<String, Script> instanceCache = new ConcurrentHashMap<>();

    private final Binding binding;
    private CompilerConfiguration compilerConfiguration = new CompilerConfiguration();

    private ScriptVariables scriptVariables = new ScriptVariables();
    private List<GroovyInterceptor> groovyInterceptors = new ArrayList<>();
    private List<CommonScriptInterceptor> scriptInvokeInterceptors = new ArrayList<>();

    public GroovyScriptManager() {
        binding = new Binding(scriptVariables.getParams());
        compilerConfiguration.addCompilationCustomizers(new SandboxTransformer());
    }

    public void invoke(String scriptName, Map<String, Object> scriptParams) {
        Script scriptInstance = getScriptInstance(scriptName);
        if (scriptInstance == null) {
            throw new RuntimeException(String.format("no such script: %s", scriptName));
        }
        beforeScriptInvoke(scriptName, scriptInstance, scriptParams);

        try {
            scriptVariables.setParams(scriptParams);
            for (GroovyInterceptor interceptor : groovyInterceptors) {
                interceptor.register();
            }
            scriptInstance.run();
        } finally {
            for (GroovyInterceptor interceptor : groovyInterceptors) {
                interceptor.unregister();
            }
            scriptParams.clear();
        }
    }

    @Override
    public Script generateScriptInstance(String scriptName, String scriptText) {
        GroovyClassLoader scriptClassLoader = AccessController
                .doPrivileged(new PrivilegedAction<GroovyClassLoader>() {
                    @Override
                    public GroovyClassLoader run() {
                        return new GroovyClassLoader(getClass().getClassLoader(), compilerConfiguration);
                    }
                });

        Class<?> clazz = scriptClassLoader.parseClass(scriptText);
        try {
            Script script = null;
            if (Script.class.isAssignableFrom(clazz)) {
                try {
                    Constructor constructor = clazz.getConstructor(Binding.class);
                    script = (Script)constructor.newInstance(binding);
                } catch (Throwable e) {
                    // Fallback for non-standard "Script" classes.
                    script = (Script)clazz.newInstance();
                    script.setBinding(binding);
                }
            } else {
                //不是脚本,return null;
                logger.warn("script:" + scriptText + " is not a script!!");
            }

            return script;
        } catch (Throwable e) {
            throw new RuntimeException(
                    "Failed to Generate scriptInstance! scriptText" + scriptText, e);
        }
    }

    @Override
    public Script getScriptInstance(String scriptId) {
        return instanceCache.get(scriptId);
    }

    @Override
    public void registerScript(String scriptId, String scriptText) {
        assertTrue(StringUtils.isNotBlank(scriptId), "scriptId is empty!");
        assertTrue(StringUtils.isNotBlank(scriptText), "scriptText is empty!");
        removeScriptId(scriptId);
        instanceCache.put(scriptId, generateScriptInstance(scriptId, scriptText));
    }

    @Override
    public void deRegisterScript(String scriptId) {
        removeScriptId(scriptId);
    }

    private void removeScriptId(String scriptId) {
        Object old = instanceCache.remove(scriptId);
        if (old == null) {
            return;
        }
        clearAllClassInfo(old.getClass());
    }

    @Override
    public void removeAllScript() {
        for (Map.Entry entry : instanceCache.entrySet()) {
            clearAllClassInfo(entry.getValue().getClass());
        }
        instanceCache.clear();
    }

    @Override
    public String getScriptState() {
        StringBuilder sb = new StringBuilder("scriptIds: ");
        for (String id : instanceCache.keySet()) {
            sb.append(id).append(File.separatorChar);
        }
        String stat = sb.toString();
        logger.info(stat);
        return stat;
    }

    @Override
    public boolean isEmpty() {
        return instanceCache.isEmpty();
    }

    public void assertTrue(boolean flag, String errMsg) {
        if (!flag) {
            throw new IllegalArgumentException(errMsg);
        }
    }

    public void clearAllClassInfo(Class<?> type) {
        try {
            GroovySystem.getMetaClassRegistry().removeMetaClass(type);
        } catch (Throwable e) {
            logger.warn("clear groovy class cache fail!" + e.getMessage());
        }
    }

    public void setCompilerConfiguration(CompilerConfiguration cc) {
        this.compilerConfiguration = cc;
    }

    public void addGroovyInterceptor(GroovyInterceptor groovyInterceptor) {
        groovyInterceptors.add(groovyInterceptor);
    }

    public void addScriptInvokeInterceptor(CommonScriptInterceptor scriptInvokeInterceptor) {
        scriptInvokeInterceptors.add(scriptInvokeInterceptor);
    }


    private void beforeScriptInvoke(String scriptName, Object scriptInstance,
                                    Map<String, Object> scriptParams) {
        try {
            for (CommonScriptInterceptor invokeInterceptor : scriptInvokeInterceptors) {
                invokeInterceptor.preHandle(scriptName, scriptInstance, scriptParams);
            }
        } catch (Exception e) {
            throw new RuntimeException("script invoke interceptors err!", e);
        }
    }
}
