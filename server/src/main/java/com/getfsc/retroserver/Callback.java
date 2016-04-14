package com.getfsc.retroserver;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/14
 * Time: 下午6:49
 */
public interface Callback<T> {
    void done(T t);

    void failed(Exception e, Object message);
}
