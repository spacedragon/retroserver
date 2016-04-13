package com.getfsc.retroserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.getfsc.retroserver.util.H;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/11
 * Time: 下午7:08
 */
public class ObjectConvert {
    private static ObjectConvert ourInstance = new ObjectConvert();
    private final ObjectMapper objectMapper;

    public static ObjectConvert getInstance() {
        return ourInstance;
    }

    private ObjectConvert() {
        objectMapper = new ObjectMapper();
    }

    public static <T> T convert(Object object, Class<T> clazz) {
        return getInstance().objectMapper.convertValue(object, clazz);
    }

    public static byte[] toJson(Object object) {
        try {
            return getInstance().objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw H.rte(e);
        }
    }
}
