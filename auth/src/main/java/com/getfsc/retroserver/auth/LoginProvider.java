package com.getfsc.retroserver.auth;

import com.getfsc.retroserver.ObjectConvert;
import com.getfsc.retroserver.http.ServerRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/14
 * Time: 上午10:45
 */
public class LoginProvider {

    public static final String USER_ROLES = "userRoles";


    public static final String LOGIN_USER = "loginUser";
    private ServerRequest req;

    public LoginProvider(ServerRequest req) {
        this.req = req;
    }

    public void login(Object loggedUser) {
        req.session().set(LOGIN_USER, loggedUser);
    }


    public void logout() {
        req.session().remove(LOGIN_USER);
    }

    public <T> T getUser(Class<T> clazz) {
        return ObjectConvert.convert(req.session().get(LOGIN_USER), clazz);
    }

    public List<String> userRoles(ServerRequest req) {
        List<String> roles = req.session().get(USER_ROLES);
        return roles == null ? Collections.emptyList() : roles;
    }

    public void setRoles(String... roles) {
        req.session().set(USER_ROLES, Arrays.asList(roles));
    }
}
