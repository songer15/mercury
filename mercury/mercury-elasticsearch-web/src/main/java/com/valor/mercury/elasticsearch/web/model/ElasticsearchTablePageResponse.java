package com.valor.mercury.elasticsearch.web.model;

import java.util.ArrayList;
import java.util.List;

public class ElasticsearchTablePageResponse<T> {
    private int code; //状态码, 0表示成功

    private String msg;  //提示信息

    private long count; // 总数量, bootstrapTable是total

    private List<String> columns = new ArrayList<>();
    private List<T> data = new ArrayList<T>();

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
