package com.getfsc.retroserver.jwt;

import com.getfsc.retroserver.http.ServerRequest;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/14
 * Time: 上午11:14
 */

public class JwtProvider {


    private JwtOptions options;
    private ServerRequest req;

    public JwtProvider(JwtOptions options, ServerRequest req) {
        this.options = options;
        this.req = req;
    }


    public HashMap<String, Object> getJwtData() {
        return (HashMap<String, Object>) req.getObject("jwt");
    }

    public String setJwt(HashMap<String, Object> data) {
        JwtBuilder jwtBuilder = Jwts.builder()
                .setClaims(data);
        long expireMills = options.jwtExpiration().toMillis();
        if (expireMills > 0)
            jwtBuilder.setExpiration(new Date(System.currentTimeMillis() + expireMills));
        jwtBuilder.setId(req.session().id());
        return jwtBuilder.signWith(SignatureAlgorithm.HS512, options.jwtSecret())
                .compact();
    }

}
