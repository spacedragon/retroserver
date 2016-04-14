package com.getfsc.retroserver.auth;

import com.getfsc.retroserver.aop.AopFactory;
import com.getfsc.retroserver.aop.AopInterceptor;
import com.getfsc.retroserver.request.ServerRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

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
                RoleProvider roleProvider = new RoleProvider(request);
                request.setObject(RoleProvider.class, roleProvider);
                LoginProvider loginProvider = request.get(LoginProvider.class);
                Object object = loginProvider.getUser();
                if (object == null) {
                    status = HttpResponseStatus.UNAUTHORIZED;
                    message = "requires login.";
                    return false;
                }
                List<String> roles = roleProvider.userRoles(request);
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
            public Response.Builder afterInvoke(ServerRequest request, Response.Builder response) {
                if (status != HttpResponseStatus.OK) {
                    return response.code(status.code()).body(ResponseBody.create(MediaType.parse("text/plain"), message));
                }
                return response;
            }
        };
    }

    @Override
    public AopFactory setFactoryParams(Object[] params) {
        checkRoles = (String[]) params;
        return this;
    }
}
