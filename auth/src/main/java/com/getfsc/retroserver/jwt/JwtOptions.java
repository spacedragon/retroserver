package com.getfsc.retroserver.jwt;

import java.time.Duration;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/13
 * Time: 下午10:45
 */
public interface JwtOptions {
    byte[] jwtSecret() ;

    Duration jwtExpiration();
}
