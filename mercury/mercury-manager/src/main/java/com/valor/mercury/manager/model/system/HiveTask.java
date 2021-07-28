package com.valor.mercury.manager.model.system;

/**
 * @author Gavin
 * 2020/10/04 13:16
 */
public class HiveTask {
    private String name;
    private String description;
    private String executor;
    private String parameterMap;
    private String sql;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(String parameterMap) {
        this.parameterMap = parameterMap;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
