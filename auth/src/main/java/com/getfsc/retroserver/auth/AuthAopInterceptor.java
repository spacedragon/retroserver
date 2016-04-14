package com.getfsc.retroserver.auth;

import com.getfsc.retroserver.aop.AopInterceptor;
import com.getfsc.retroserver.request.ServerRequest;
import okhttp3.Response;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/14
 * Time: 上午10:54
 */
class AuthAopInterceptor implements AopInterceptor {
    private LoginProvider provider;

    public AuthAopInterceptor(LoginProvider provider) {
        this.provider = provider;
    }

    @Override
    public boolean beforeInvoke(ServerRequest request) {
        request.setObject(LoginProvider.class,provider);
        return true;
    }

    @Override
    public Response.Builder afterInvoke(ServerRequest request, Response.Builder response) {
        return null;
    }

    @Override
    public void destory() {

    }
}
