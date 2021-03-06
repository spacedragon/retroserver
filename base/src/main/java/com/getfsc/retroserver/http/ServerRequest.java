package com.getfsc.retroserver.http;

import okhttp3.RequestBody;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/11
 * Time: 下午6:44
 */
public interface ServerRequest {

    Value path(String key);

    Value field(String key);

    Value query(String key);

    RequestBody part(String key);

    Value header(String key);


    String uri();

    String verb();

    <T> T body(Class<T> clz);

    Map<String, String> queryMap();

    void setObject(Object key, Object value);

    Object getObject(Object key);

    default <T> T get(Class<T> clz){
        return (T) getObject(clz);
    };

    Session session();

    <T> ServerResponse<T> response();
}
