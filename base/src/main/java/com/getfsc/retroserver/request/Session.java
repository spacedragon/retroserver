package com.getfsc.retroserver.request;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/13
 * Time: 下午9:37
 */
public interface Session {


    <T> T get(String key);

    <T> void set(String key, T value);

    String id();

    void remove(String key);
}
