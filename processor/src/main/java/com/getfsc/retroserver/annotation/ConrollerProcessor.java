package com.getfsc.retroserver.annotation;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/11
 * Time: 上午9:51
 */

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

/**
 * Annotation processor for Complexity annotation type.
 *
 * @author deors
 * @version 1.0
 */
@SupportedAnnotationTypes("com.getfsc.retroserver.annotation.Controller")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ConrollerProcessor
        extends AbstractProcessor {

    /**
     * Default constructor.
     */
    public ConrollerProcessor() {

        super();
    }

    private HashMap<String, ControllerGenerator> generators = new HashMap<>();

    /**
     * Reads the complexity value contained in the annotation and prints it in the console
     * (NOTE level).
     *
     * @param annotations set of annotations found
     * @param roundEnv    the environment for this processor round
     * @return whether a new processor round would be needed
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element elem : roundEnv.getElementsAnnotatedWith(Bootstrap.class)) {
            Bootstrap bootstrap = elem.getAnnotation(Bootstrap.class);
            String pkg = bootstrap.value();
            if (pkg.isEmpty()) {
                TypeElement classElement = (TypeElement) elem;
                PackageElement packageElement =
                        findPackage(classElement);
                pkg = packageElement.getQualifiedName().toString();
                generators.put(pkg, new ControllerGenerator(pkg,classElement,processingEnv));
            }
        }
        for (Element elem : roundEnv.getElementsAnnotatedWith(Controller.class)) {

            TypeElement classElement = (TypeElement) elem;
            PackageElement packageElement =
                    findPackage(classElement);
            String pkg = packageElement.getQualifiedName().toString();
            for (String key : generators.keySet()) {
                if (key.startsWith(pkg)) {
                    ControllerGenerator generator = generators.get(key);
                    generator.addControllerElem(classElement);
                }
            }
        }
        for (ControllerGenerator generator : generators.values()) {

            try {
                generator.build(processingEnv);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public PackageElement findPackage(Element classElement) {

        Element  parent = classElement.getEnclosingElement();
        if (parent.getKind() == ElementKind.PACKAGE) {
            return (PackageElement) parent;
        }else {
            return findPackage(parent);
        }
    }
}
