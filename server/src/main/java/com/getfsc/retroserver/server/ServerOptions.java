package com.getfsc.retroserver.server;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/11
 * Time: 下午2:37
 */
public interface ServerOptions {
    default int getPort(){
        return 8080;
    };
    default boolean ssl(){
        return false;
    }
}
