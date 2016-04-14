package com.getfsc.retroserver.request;

import com.getfsc.retroserver.ObjectConvert;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/12
 * Time: 上午1:32
 */
public interface Value {

    Object value();

    default <T> T get(Class<T> clazz) {
        return ObjectConvert.convert(value(), clazz);
    }


    default <T> T getOrDefault(T defaultValue) {
        if (value() == null) {
            return defaultValue;
        } else {
            return get((Class<T>) defaultValue.getClass());
        }
    }
}
