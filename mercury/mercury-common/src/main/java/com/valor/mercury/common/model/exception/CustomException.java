package com.valor.mercury.common.model.exception;

public class CustomException extends Exception  {

    //100：数据错误
    //101：数据数量错误
    //103：配置错误
    //201：写入es时发生异常（IO异常）
    //202：读取mapping映射文件异常（IO异常）
    //203：判断es索引是否存在时异常（IO异常）
    //204：创建es索引时异常（IO异常）
    //205：数据转换异常
    private int code;
    //private String msg;

    public CustomException(int code, String msg) {
        super(msg);
        this.code = code;
        //this.msg = msg;
    }
    public CustomException(int code, Exception e){
        super(e);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return getMessage();
    }

}
