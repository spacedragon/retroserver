package com.getfsc.retroserver.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/12
 * Time: 下午6:34
 */

@Retention(RUNTIME)
@Target(METHOD)
public @interface ServerHeaders {
    String[] value();
}
