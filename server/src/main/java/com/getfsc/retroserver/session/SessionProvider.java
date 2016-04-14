package com.getfsc.retroserver.session;

import com.getfsc.retroserver.http.ServerRequest;
import com.getfsc.retroserver.http.Session;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaderNames.COOKIE;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/13
 * Time: 下午9:34
 */
public abstract class SessionProvider {

    private static final String JSESSIONID = "jsessionid";


    public Session getSession(ServerRequest request) {
        Session session = (Session) request.getObject(Session.class);
        if (session == null) {
            String cookieString = request.header(COOKIE.toString()).toString();
            if (cookieString != null) {
                Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieString);
                if (!cookies.isEmpty()) {
                    for (Cookie cookie : cookies) {
                        if (cookie.name().equals(JSESSIONID)) {
                            String value = cookie.value();
                            session = load(value);
                            request.setObject(Session.class, session);
                        }
                    }
                }
            }
            if (session == null) {
                session = newSession();
                request.setObject(Session.class, session);
            }
        }
        return session;
    }


    public abstract Session newSession();


    public abstract Session load(String value);


}