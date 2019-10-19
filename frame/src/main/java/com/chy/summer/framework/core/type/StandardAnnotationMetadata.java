package com.chy.summer.framework.core.type;

import com.chy.summer.framework.core.annotation.AnnotationAttributes;

import java.lang.annotation.Annotation;

public class StandardAnnotationMetadata extends StandardClassMetadata implements AnnotationMetadata {

    private final Annotation[] annotations;

    private final boolean nestedAnnotationsAsMap;


    public StandardAnnotationMetadata(Class<?> introspectedClass, boolean nestedAnnotationsAsMap) {
        super(introspectedClass);
        this.annotations = introspectedClass.getAnnotations();
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
    }

    @Override
    public boolean hasMetaAnnotation(String annotationName) {
        return false;
    }

    @Override
    public boolean hasAnnotation(String metaAnnotationName) {
        return false;
    }

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public boolean isIndependent() {
        return false;
    }

    @Override
    public boolean isConcrete() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public AnnotationAttributes getAnnotationAttributes(Class<? extends Annotation> type) {
        return null;
    }

    @Override
    public boolean isAnnotated(String name) {
        return false;
    }
}
