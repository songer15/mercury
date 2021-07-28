package com.valor.mercury.common.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;



public class JsonUtils {

    public static ObjectMapper objectMapper=new ObjectMapper();


    public static String toJsonString(Object object)  {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return null;
        }
    }


}
