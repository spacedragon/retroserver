package com.getfsc.retroserver.jwt;

import com.getfsc.retroserver.aop.AopFactory;
import com.getfsc.retroserver.aop.AopInterceptor;
import com.getfsc.retroserver.http.ServerRequest;
import com.getfsc.retroserver.http.Session;
import com.getfsc.retroserver.session.SessionProvider;
import com.getfsc.retroserver.util.H;
import io.jsonwebtoken.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/13
 * Time: 下午8:11
 */


public class JwtAopFactory implements AopFactory {

    private static final String PREFIX = "Bearer ";

    private JwtParser jwtParser;
    private JwtOptions options;


    @Inject
    public JwtAopFactory(JwtOptions options) {
        this.options = options;
        jwtParser = Jwts.parser().setSigningKey(options.jwtSecret());
    }

    @Override
    public String name() {
        return "JWT";
    }

    @Override
    public AopInterceptor create() {
        return new JwtAopInterceptor();
    }

    @Override
    public JwtAopFactory setFactoryParams(Object[] params) {
        return this;
    }

    private class JwtAopInterceptor implements AopInterceptor {

        private int code = 0;
        private String message = "";

        @Override
        public boolean beforeInvoke(ServerRequest req) {
            req.setObject(JwtProvider.class, new JwtProvider(options, req));
            String header = req.header("authorization").getOrDefault(req.header("Authorization").get(String.class));

            if (!H.isEmpty(header) && header.startsWith(PREFIX)) {
                String jwtstr = header.substring(PREFIX.length());
                Jws<Claims> jwt;
                try {
                    jwt = jwtParser.parseClaimsJws(jwtstr);
                } catch (ExpiredJwtException e) {
                    code = 410;
                    message = "authorization expired.";
                    return false;
                } catch (JwtException e) {
                    code = 403;
                    message = e.getMessage();
                    return false;
                }
                if (jwt == null) {
                    code = 403;
                    message = "jwt is missiong";
                    return false;
                }
                HashMap<String,Object> map = new HashMap<>();
                map.putAll(jwt.getBody());

                Session session = (Session) req.getObject(Session.class);


                if (session == null) {
                    String sessionId = jwt.getBody().getId();
                    SessionProvider sessionProvider = req.get(SessionProvider.class);
                    if (sessionProvider != null) {
                        session = sessionProvider.load(req,sessionId);
                        if (session == null) {
                            session = sessionProvider.newSession(sessionId);
                            session.putAll(map);
                        }else {
                            for (Map.Entry<String,Object> entry : map.entrySet()) {
                                if (!session.has(entry.getKey())) {
                                    session.set(entry.getKey(), entry.getValue());
                                }
                            }
                        }
                    }
                }
                req.setObject("jwt", map);

            }

            return true;
        }

        @Override
        public void afterInvoke(ServerRequest request) {
            if (code > 0) {
                request.response().error(code, message);
            }
        }


    }
}
