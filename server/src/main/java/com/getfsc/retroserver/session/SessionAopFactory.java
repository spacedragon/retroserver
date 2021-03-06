package com.getfsc.retroserver.session;

import com.getfsc.retroserver.aop.AopFactory;
import com.getfsc.retroserver.aop.AopInterceptor;
import com.getfsc.retroserver.http.ServerRequest;
import com.getfsc.retroserver.http.Session;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import okhttp3.Response;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaderNames.COOKIE;
import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/13
 * Time: 下午9:31
 */


public class SessionAopFactory implements AopFactory {

    private SessionProvider sessionProvider;

    @Inject
    public SessionAopFactory(SessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    private static final String JSESSIONID = "jsessionid";

    @Override
    public String name() {
        return "SESSION";
    }

    @Override
    public AopInterceptor create() {
        return new AopInterceptor() {
            @Override
            public boolean beforeInvoke(ServerRequest request) {
                request.setObject(SessionProvider.class, sessionProvider);
                return true;
            }

            @Override
            public void afterInvoke(ServerRequest request) {
                Session session = (Session) request.getObject(Session.class);
                if (session != null) {
                    String cookieString = request.header(COOKIE.toString()).toString();
                    if (cookieString != null) {
                        Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieString);
                        if (!cookies.isEmpty()) {
                            Optional<Cookie> cookie = cookies.stream().filter(c -> c.name().equals(JSESSIONID))
                                    .findFirst();
                            if (!cookie.isPresent()) {
                                cookies.add(new DefaultCookie(JSESSIONID, session.id()));
                                ServerCookieEncoder.STRICT.encode(cookies)
                                        .stream().forEach(value -> request.response().addHeader(SET_COOKIE.toString(), value));
                            }
                        }
                    }
                }
            }

        };
    }

    @Override
    public Integer priority() {
        return 1000;
    }

    @Override
    public AopFactory setFactoryParams(Object[] params) {
        return this;
    }
}
