package com.valor.mercury.gateway.model.dto;

import java.io.Serializable;

public class QueryResponseDTO<T> implements Serializable {

    private int code;

    private T data;

    private String message;



    public QueryResponseDTO(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    private static int SUCCESS = 0000;
    private static int ERROR = 9999;

    public static QueryResponseDTO success(Object data) {
        return new QueryResponseDTO(SUCCESS, data, null);
    }

    public static QueryResponseDTO fail(Object data) {
        return new QueryResponseDTO(ERROR, data, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
