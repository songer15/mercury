package com.valor.mercury.common.model;

import java.util.Map;

public class WrapperEntity extends AbstractPrintable {
    private String type;
    private Map<String, Object> object;

    public WrapperEntity() {
    }

    public WrapperEntity(String type, Map<String, Object> object) {
        this.type = type;
        this.object = object;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getObject() {
        return object;
    }

    public void setObject(Map<String, Object> object) {
        this.object = object;
    }
}
