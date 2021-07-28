package com.valor.mercury.gateway.model.dto;

import java.io.Serializable;

public class QueryRequestDTO extends BaseDTO implements Serializable {

    private String index;

    private String sql;

    private String filePath;

    public String method = "SQL";

    public long dataSize;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getDataSize() {
        return dataSize;
    }

    public void setDataSize(long dataSize) {
        this.dataSize = dataSize;
    }
}
