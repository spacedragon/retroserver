package com.getfsc.retroserver.jwt;

import com.getfsc.retroserver.annotation.AnnoProcessor;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/13
 * Time: 下午7:48
 */
public class JwtProcessor implements AnnoProcessor<JWT> {
    @Override
    public void process(ProcessingEnvironment processingEnv, JWT annotation, MethodSpec.Builder routeStatments, MethodSpec.Builder callStatments) {
         installAop(routeStatments,"JWT", CodeBlock.builder().build());
    }


}
