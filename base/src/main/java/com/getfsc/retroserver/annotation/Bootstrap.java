package com.getfsc.retroserver.annotation;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/11
 * Time: 下午4:52
 */
@Documented
@Target({
        ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Bootstrap {
    String value() default "";
}
