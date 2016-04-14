package com.getfsc.retroserver.annotation;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/13
 * Time: 下午9:15
 */
@Qualifier
@Retention(RUNTIME)
public @interface GlobalAop {
}
