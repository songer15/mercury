package com.valor.mercury.gateway.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonUtils {

    public static ObjectMapper objectMapper=new ObjectMapper();


    public static String toJsonString(Object object) throws JsonProcessingException {
            return objectMapper.writeValueAsString(object);
    }

//    public static <T> T toObjectList(ParameterizedType s, String value) throws CustomException {
//        try {
//            return objectMapper.readValue(value, new TypeReference<>(){});
//            //Arrays.asList()
//            //objectMapper.readValue(value, JavaType)
//        } catch (IOException e) {
//            throw new CustomException(Constants.CustomCode.JSON_DESERIALIZE.getValue(), e);
//        }
//    }
//
//    public static <T> T toObject(Reader reader) throws CustomException {
//        try {
//            return objectMapper.readValue(reader, new TypeReference<T>() {});
//        } catch (IOException e) {
//            throw new CustomException(Constants.CustomCode.JSON_DESERIALIZE.getValue(), e);
//        }
//    }

}
