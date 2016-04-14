package com.getfsc.retroserver.auth;

import com.getfsc.retroserver.request.ServerRequest;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/14
 * Time: 上午10:45
 */
public class LoginProvider {


    private static final String LOGIN_USER = "loginUser";
    private ServerRequest req;

    public LoginProvider(ServerRequest req) {
        this.req = req;
    }

    public void login(Object loggedUser){
        req.session().set(LOGIN_USER,loggedUser);
    }


    public void logout() {
        req.session().remove(LOGIN_USER);
    }

    public <T> T getUser() {
        return req.session().get(LOGIN_USER);
    }
}
