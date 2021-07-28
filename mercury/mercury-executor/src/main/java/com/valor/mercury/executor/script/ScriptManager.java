package com.valor.mercury.executor.script;


import java.util.Map;

public interface ScriptManager <T> {

    /**
     * 登记
     *
     * @param scriptId   不能为null,属性不能为空,否则参数校验异常
     * @param scriptText 不能为空,否则参数校验异常
     */
    void registerScript(String scriptId, String scriptText);

    /**
     * 注销
     *
     * @param scriptId
     */
    void deRegisterScript(String scriptId);

    /**
     * 强制销毁所有脚本
     */
    void removeAllScript();

    String getScriptState();

    /**
     * 在管理的脚本实例是否为空
     *
     * @return
     */
    boolean isEmpty();

    /**
     * 生成脚本对象
     * @param scriptName
     * @param scriptText
     * @return
     */

    T generateScriptInstance(String scriptName, String scriptText);

    /***
     * 根据脚本Id 获取脚本实例
     *
     * @param scriptId 脚本唯一标识
     * @return
     */
    T getScriptInstance(String scriptId);

    /**
     * 执行入口
     *
     * @param scriptName   脚本Id,
     * @param scriptParams 脚本执行的参数[参数名--参数实例]
     * @param <T>          可以为null
     * @return
     */
    void invoke(String scriptName, Map<String, Object> scriptParams);
}
