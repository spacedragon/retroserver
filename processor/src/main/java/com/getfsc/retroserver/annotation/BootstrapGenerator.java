package com.getfsc.retroserver.annotation;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import dagger.Module;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/16
 * Time: 下午12:07
 */
public class BootstrapGenerator {

    public static void build(ProcessingEnvironment processingEnv, TypeElement classElement, List<String> controllers) throws IOException {
        List<TypeElement> modules = controllers.stream().map(c ->
                processingEnv.getElementUtils().getTypeElement(c + "Module"))
                .filter(m -> m != null)
                .collect(Collectors.toList());
        String pkg = processingEnv.getElementUtils().getPackageOf(classElement).getQualifiedName().toString();

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("ControllerModule")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Module.class)
                        .addMember("includes", "{$L}",
                                modules.stream()
                                        .map(s -> s.getQualifiedName().toString() + ".class")
                                        .collect(Collectors.joining(",\n")))
                        .build());

        String fullName = pkg + ".ControllerModule";
        if (processingEnv.getElementUtils().getTypeElement(fullName) != null) {
            return;
        }

        JavaFile javaFile = JavaFile.builder(pkg, classBuilder.build())
                .build();
        JavaFileObject jfo = processingEnv.getFiler().createSourceFile(fullName);
        try (Writer writer = jfo.openWriter()) {
            javaFile.writeTo(writer);
        }

    }
}
