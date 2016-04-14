package com.getfsc.retroserver.auth;

import com.getfsc.retroserver.annotation.AnnoProcessor;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/13
 * Time: 下午5:21
 */
public class CheckProcessor implements AnnoProcessor<Check> {

    @Override
    public void process(ProcessingEnvironment processingEnv, Check annotation, MethodSpec.Builder routeStatments, MethodSpec.Builder callStatments) {

        String[] values = annotation.value();
        String params =Arrays.asList(values).stream().map(s -> of("$S", s).toString())
                .collect(Collectors.joining(", "));

        installAop(routeStatments, "AuthCheck", of("new String[]{$L}",params));
    }

    private CodeBlock of(String format, Object...args) {
        return CodeBlock.builder().add(format, args).build();
    }
}
