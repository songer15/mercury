package com.vms.metric.analyse.model;


public class ResponseEntity<T> {

    private int code;
    private String msg;
    private T data;

    private ResponseEntity<T> setCode(int code) {
        this.code = code;
        return this;
    }


    private ResponseEntity<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }


    private ResponseEntity<T> setData(T data) {
        this.data = data;
        return this;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    public static <T> ResponseEntity<T> success(T data) {
        return new ResponseEntity<T>().setCode(200).setMsg("SUCCESS").setData(data);
    }

    public static ResponseEntity success(){
        return new ResponseEntity().setMsg("SUCCESS").setCode(200);
    }


    public static <T> ResponseEntity<T> fail(String msg,int code, T failedData) {
        return new ResponseEntity<T>().setMsg(msg).setCode(code).setData(failedData);
    }

    public static  ResponseEntity fail(String msg,int code) {
        return new ResponseEntity().setMsg(msg).setCode(code);
    }
}
