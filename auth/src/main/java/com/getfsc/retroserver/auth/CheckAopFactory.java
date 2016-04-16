package com.getfsc.retroserver.auth;

import com.getfsc.retroserver.aop.AopFactory;
import com.getfsc.retroserver.aop.AopInterceptor;
import com.getfsc.retroserver.http.ServerRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/14
 * Time: 下午12:08
 */
class CheckAopFactory implements AopFactory {
    public String[] checkRoles;

    @Override
    public String name() {
        return "AuthCheck";
    }


    private HttpResponseStatus status = HttpResponseStatus.OK;
    private String message = "";

    @Override
    public AopInterceptor create() {
        return new AopInterceptor() {
            @Override
            public boolean beforeInvoke(ServerRequest request) {

                LoginProvider loginProvider = request.get(LoginProvider.class);
                Object object = loginProvider.getUser();
                if (object == null) {
                    status = HttpResponseStatus.UNAUTHORIZED;
                    message = "requires login.";
                    return false;
                }
                List<String> roles = loginProvider.userRoles(request);
                for (String checkRole : checkRoles) {
                    if (roles.contains(checkRole)) {
                        return true;
                    }
                }
                status = HttpResponseStatus.FORBIDDEN;
                message = "permission denied.";

                return false;
            }

            @Override
            public void afterInvoke(ServerRequest request) {
                if (status != HttpResponseStatus.OK) {
                    request.response().code(status.code()).setBody(message);
                }
            }
        };
    }

    @Override
    public AopFactory setFactoryParams(Object[] params) {
        checkRoles = (String[]) params;
        return this;
    }
}
