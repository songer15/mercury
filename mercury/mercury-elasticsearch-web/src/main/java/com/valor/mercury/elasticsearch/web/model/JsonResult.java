package com.valor.mercury.elasticsearch.web.model;
import java.util.HashMap;


public class JsonResult extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;
	
    public JsonResult() {
    }

    /**
     * 返回成功
     */
    public static JsonResult ok() {
        return ok("Operation is successful");
    }

    /**
     * 返回成功
     */
    public static JsonResult ok(String message) {
        return ok(200, message);
    }

    /**
     * 返回成功
     */
    public static JsonResult ok(int code, String message) {
        JsonResult jsonResult = new JsonResult();
        jsonResult.put("code", code);
        jsonResult.put("msg", message);
        return jsonResult;
    }

    /**
     * 返回失败
     */
    public static JsonResult error() {
        return error("The operation failure");
    }

    /**
     * 返回失败
     */
    public static JsonResult error(String messag) {
        return error(500, messag);
    }

    /**
     * 返回失败
     */
    public static JsonResult error(int code, String message) {
        return ok(code, message);
    }

    public static JsonResult error(int retCode,int errorId,String errorMsg){
        return error(500,retCode+"-"+errorId+","+errorMsg);
    }

    /**
     * 设置code
     */
    public JsonResult setCode(int code) {
        super.put("code", code);
        return this;
    }

    /**
     * 设置message
     */
    public JsonResult setMessage(String message) {
        super.put("msg", message);
        return this;
    }

    /**
     * 放入object
     */
    @Override
    public JsonResult put(String key, Object object) {
        super.put(key, object);
        return this;
    }
}