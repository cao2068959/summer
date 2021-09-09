package com.chy.summer.framework.core.annotation;


import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public class AnnotatedElementForAnnotations implements AnnotatedElement {
    private final Annotation[] annotations;

    public AnnotatedElementForAnnotations(Annotation... annotations) {
        this.annotations = annotations;
    }


    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {

        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == annotationClass){
                return (T) annotation;
            }
        }
        return null;
    }

    @Override
    public Annotation[] getAnnotations() {
        return (Annotation[])this.annotations.clone();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return (Annotation[])this.annotations.clone();
    }
}
