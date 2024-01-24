package com.raidiam.trustframework.bank.utils;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.annotation.*;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;

import java.lang.annotation.Annotation;
import java.util.Map;

public class AnnotationsUtil {

    private static final Map<Class<? extends Annotation>, HttpMethod> ANNOTATION_METHOD_MAP = Map.of(
            Get.class, HttpMethod.GET,
            Post.class, HttpMethod.POST,
            Put.class, HttpMethod.PUT,
            Patch.class, HttpMethod.PATCH,
            Delete.class, HttpMethod.DELETE
    );

    public interface AnnotationAction {
        void perform(String fullPath, HttpMethod httpMethod, AnnotationValue<?> extractedAnnotation);
    }

    public static void performActionsOnControllerMethodByAnnotation(ApplicationContext applicationContext, Class<? extends Annotation> annotationToExtract, AnnotationAction annotationAction) {
        applicationContext.getBeanDefinitions(Qualifiers.byStereotype(Controller.class))
                .forEach(controllerClass -> controllerClass.getExecutableMethods()
                        .forEach(controllerMethod -> controllerMethod.findAnnotation(annotationToExtract)
                                .ifPresent(extractedAnnotation -> controllerClass.findAnnotation(Controller.class)
                                        .ifPresent(controllerAnnotation -> {
                                            String controllerPath = controllerAnnotation.stringValue("value").orElse("/");
                                            addAnnotationPath(controllerMethod, controllerPath, extractedAnnotation, annotationAction);
                                        }))));
    }


    private static void addAnnotationPath(ExecutableMethod<?, ?> controllerMethod, String controllerPath,
                                          AnnotationValue<?> extractedAnnotation, AnnotationAction annotationAction) {
        ANNOTATION_METHOD_MAP.forEach((annotation, httpMethod) ->
                controllerMethod.findAnnotation(annotation).ifPresent(get -> {
                    String methodPath = get.stringValue("value").orElse("/");
                    String fullPath = decoratePath(controllerPath + methodPath);
                    annotationAction.perform(fullPath, httpMethod, extractedAnnotation);
                }));
    }

    private static String decoratePath(String path) {
        path = path.replaceAll("v\\{[a-zA-Z]+\\}", "v([0-9]*)");
        path = path.replaceAll("\\{[a-zA-Z]+\\}", "([a-zA-Z0-9-:]*)");
        return "^" + path + "$";
    }
}
