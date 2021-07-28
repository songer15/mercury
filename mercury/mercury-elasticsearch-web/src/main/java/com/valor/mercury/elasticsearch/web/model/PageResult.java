package com.valor.mercury.elasticsearch.web.model;
import java.util.List;


public class PageResult<T> {

    private int code; //状态码, 0表示成功

    private String msg;  //提示信息

    private long count; // 总数量, bootstrapTable是total

    private List<T> data; // 当前数据, bootstrapTable是rows


    public PageResult() {
    }

    public PageResult(List<T> rows) {
        this.data = rows;
        this.count = rows.size();
        this.code = 0;
        this.msg = "";
    }

    public PageResult(long total, List<T> rows) {
        this.count = total;
        this.data = rows;
        this.code = 0;
        this.msg = "";
    }

    public static PageResult error(int code, String message) {
        return ok(code, message);
    }

    public static PageResult error(int retCode,int errorId,String errorMsg){
        return error(500,retCode+"-"+errorId+","+errorMsg);
    }


    public static PageResult ok(int code, String message) {
        PageResult pageResult = new PageResult();
        pageResult.setCode(code);
        pageResult.setMsg(message);
        return pageResult;
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

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
        this.count = data.size();
    }
}
