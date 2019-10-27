package com.chy.summer.framework.core.type;

import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StandardAnnotationMetadata extends StandardClassMetadata implements AnnotationMetadata {

    private final Annotation[] annotations;

    private final boolean nestedAnnotationsAsMap;

    private final Map<String,AnnotationAttributes> annotationAttributesMap;

    //注解的继承关系
    private final Map<String, Set<String>> annotationTree;

    public StandardAnnotationMetadata(Class<?> introspectedClass, boolean nestedAnnotationsAsMap) {
        super(introspectedClass);
        this.annotations = introspectedClass.getAnnotations();
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
        annotationAttributesMap = new HashMap<>();
        //解析类上的注解
        annotationTree = AnnotationUtils.getAnnotationInfoByClass(introspectedClass,annotationAttributesMap,true);
    }

    @Override
    public boolean hasMetaAnnotation(String annotationName) {
        return annotationAttributesMap.containsKey(annotationName);
    }

    @Override
    public boolean hasAnnotation(String metaAnnotationName) {
        return false;
    }



    @Override
    public AnnotationAttributes getAnnotationAttributes(Class<? extends Annotation> type) {
        return annotationAttributesMap.get(type.getName());
    }

    @Override
    public boolean hasAnnotatedMethods(String name) {
        return false;
    }

    @Override
    public Set<String> getAnnotationTypes() {
        return annotationTree.keySet();
    }

    @Override
    public Set<MethodMetadata> getAnnotatedMethods(String name) {
        return null;
    }

    public boolean isAnnotated(String name) {
        return false;
    }
}
