package com.getfsc.retroserver.annotation;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/11
 * Time: 上午9:51
 */

import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Annotation processor for Complexity annotation type.
 *
 * @author deors
 * @version 1.0
 */
@SupportedAnnotationTypes({"com.getfsc.retroserver.annotation.Controller", "com.getfsc.retroserver.annotation.Bootstrap"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ControllerProcessor
        extends AbstractProcessor {

    public static final String CONTROLLER_INDEX = "META-INF/controllers/index";

    private IndexWriter indexWriter;
    public Set<String> controllers = new HashSet<>();

    private boolean indexWrote = false;
    public Set<? extends Element> bootstrapElems;


    /**
     * Default constructor.
     */
    public ControllerProcessor() {

        super();
    }


    private HashMap<String, ControllerGenerator> generators = new HashMap<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        indexWriter = new IndexWriter(processingEnv.getFiler());
    }

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
        try {
            boolean controllerAdded = false;
            if(bootstrapElems==null)
                bootstrapElems = roundEnv.getElementsAnnotatedWith(Bootstrap.class);
            for (Element elem : roundEnv.getElementsAnnotatedWith(Controller.class)) {

                TypeElement classElement = (TypeElement) elem;
                String qualifiedName = classElement.getQualifiedName().toString();
                if (!controllers.contains(qualifiedName)) {
                    PackageElement packageElement =
                            processingEnv.getElementUtils().getPackageOf(classElement);

                    String pkg = packageElement.getQualifiedName().toString();
                    ControllerGenerator generator = generators.get(pkg);
                    if (generator == null) {
                        generator = new ControllerGenerator(pkg, processingEnv);
                        generators.put(pkg, generator);
                    }
                    TypeSpec clazz = generator.addControllerElem(classElement);
                    generator.build(clazz, pkg, processingEnv);
                    controllers.add(qualifiedName);
                    controllerAdded = true;

                }
            }

            if (controllerAdded) {
                return true;
            } else if (!indexWrote) {
                indexWriter.writeSimpleNameIndexFile(controllers, CONTROLLER_INDEX);
                indexWrote = true;
                return true;
            } else {
                for (Element elem : bootstrapElems) {
                    Bootstrap bootstrap = elem.getAnnotation(Bootstrap.class);
                    List<Pattern> filters = Arrays.asList(bootstrap.value())
                            .stream()
                            .map(Pattern::compile)
                            .collect(Collectors.toList());
                    Set<String> controllers = readIndexFile(this.getClass().getClassLoader(), CONTROLLER_INDEX);
                    controllers.addAll(this.controllers);

                    List<String> filtered = controllers.stream().filter(c ->
                            filters.stream().anyMatch(f -> f.matcher(c).find()))
                            .collect(Collectors.toList());
                    TypeElement classElement = (TypeElement) elem;

                    BootstrapGenerator.build(processingEnv,classElement, filtered);
                }
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Set<String> readIndexFile(ClassLoader classLoader, String resourceFile) {
        Set<String> entries = new HashSet<>();

        try {
            Enumeration<URL> resources = classLoader.getResources(resourceFile);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream(), "UTF-8"))) {

                    String line = reader.readLine();
                    while (line != null) {
                        entries.add(line);
                        line = reader.readLine();
                    }
                } catch (FileNotFoundException e) {
                    // When executed under Tomcat started from Eclipse with "Serve modules without
                    // publishing" option turned on, getResources() method above returns the same
                    // resource two times: first with incorrect path and second time with correct one.
                    // So ignore the one which does not exist.
                    // See: https://github.com/atteo/classindex/issues/5
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("ClassIndex: Cannot read class index", e);
        }
        return entries;
    }



}
