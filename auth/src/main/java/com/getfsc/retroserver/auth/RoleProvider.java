package com.getfsc.retroserver.auth;

import com.getfsc.retroserver.http.ServerRequest;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/14
 * Time: 上午10:47
 */
public class RoleProvider {

    private static final String USER_ROLES = "userRoles";
    private ServerRequest request;

    public RoleProvider(ServerRequest request) {
        this.request = request;
    }

    public List<String> userRoles(ServerRequest req){
        return req.session().get(USER_ROLES);
    };

    public void setRoles(List<String> roles) {
        request.session().set(USER_ROLES,roles);
    }
}
