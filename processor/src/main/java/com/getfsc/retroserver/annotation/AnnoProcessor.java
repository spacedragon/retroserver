package com.getfsc.retroserver.annotation;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/13
 * Time: 下午4:42
 */
public interface AnnoProcessor<T> {
    void process(ProcessingEnvironment processingEnv, T annotation, MethodSpec.Builder routeStatments, MethodSpec.Builder callStatments);

    default void installAop(MethodSpec.Builder routeStatments, String name, CodeBlock params) {
        if (params.isEmpty()) {
            routeStatments.addStatement("route.addAop($S)", name);
        } else {
            routeStatments.addStatement("route.addAop($S,$L)", name, params);
        }

    }
}
