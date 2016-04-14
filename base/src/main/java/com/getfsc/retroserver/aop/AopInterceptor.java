package com.getfsc.retroserver.aop;


import com.getfsc.retroserver.http.ServerRequest;
import okhttp3.Response;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/13
 * Time: 下午6:43
 */
public interface AopInterceptor {

    /**
     * @param request the received request
     * @return true if should continue to invoke controller method.
     */
    boolean beforeInvoke(ServerRequest request);

    default void afterInvoke(ServerRequest request) {
    }


    default void destory() {
    }

    ;
}
