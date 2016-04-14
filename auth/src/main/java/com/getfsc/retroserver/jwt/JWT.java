package com.getfsc.retroserver.jwt;

import com.getfsc.retroserver.annotation.AnnoProcess;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/13
 * Time: 下午7:47
 */

@AnnoProcess(JwtProcessor.class)
@Retention(RUNTIME)
@Target(METHOD)
public @interface JWT {
}
