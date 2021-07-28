package com.valor.mercury.common.model;

import java.util.Map;

public class MetricMessage {
    private String name;
    private Map<String, Object> fieldsMap;

    public MetricMessage() {
    }

    public MetricMessage(String name, Map<String, Object> fieldsMap) {
        super();
        this.name = name;
        this.fieldsMap = fieldsMap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getFieldsMap() {
        return fieldsMap;
    }

    public void setFieldsMap(Map<String, Object> fieldsMap) {
        this.fieldsMap = fieldsMap;
    }

}
