package com.getfsc.retroserver;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.getfsc.retroserver.util.H;

import java.io.IOException;

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
        objectMapper.setBase64Variant(Base64Variants.MODIFIED_FOR_URL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new AfterburnerModule());
    }

    public static <T> T convert(Object object, Class<T> clazz) {
        return getInstance().objectMapper.convertValue(object, clazz);
    }

    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        try {
            return getInstance().objectMapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            throw H.rte(e);
        }
    }

    public static byte[] toJson(Object object) {
        try {
            return getInstance().objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw H.rte(e);
        }
    }
}
