package com.getfsc.retroserver.jwt;

import com.getfsc.retroserver.aop.AopFactory;
import com.getfsc.retroserver.aop.AopInterceptor;
import com.getfsc.retroserver.request.ServerRequest;
import com.getfsc.retroserver.request.Session;
import com.getfsc.retroserver.session.SessionProvider;
import com.getfsc.retroserver.util.H;
import io.jsonwebtoken.*;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;

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
            req.setObject(JwtProvider.class,new JwtProvider(options,req));
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
                HashMap map = new HashMap();
                map.putAll(jwt.getBody());

                Session session = (Session) req.getObject(Session.class);


                if (session == null) {
                    String sessionId = jwt.getBody().getId();
                    SessionProvider sessionProvider = req.get(SessionProvider.class);
                    if (sessionProvider != null) {
                        sessionProvider.load(sessionId);
                    }
                }
                req.setObject("jwt", map);

            }

            return true;
        }

        @Override
        public Response.Builder afterInvoke(ServerRequest request, Response.Builder response) {
            if (code > 0) {
                return response.code(code).body(ResponseBody.create(MediaType.parse("text/plain"), message));
            }
            return response;
        }



    }
}
