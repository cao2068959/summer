package com.chy.summer.framework.core.type;

import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.annotation.AnnotationUtils;
import com.chy.summer.framework.util.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StandardMethodMetadata implements MethodMetadata {

    private final boolean nestedAnnotationsAsMap;
    private Method method;

    private final Map<String, AnnotationAttributes> annotationAttributesMap;

    //注解的继承关系
    private final Map<String, Set<String>> annotationTree;

    public StandardMethodMetadata(Method method, boolean nestedAnnotationsAsMap) {
        this.method = method;
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
        annotationAttributesMap = new HashMap<>();
        annotationTree = AnnotationUtils.getAnnotationInfoByMethod(method, annotationAttributesMap);
    }

    @Override
    public String getMethodName() {
        return method.getName();
    }

    @Override
    public String getDeclaringClassName() {
        return method.getDeclaringClass().getName();
    }

    @Override
    public String getReturnTypeName() {
        return method.getReturnType().getName();
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(this.method.getModifiers());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(this.method.getModifiers());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(this.method.getModifiers());
    }

    @Override
    public boolean isOverridable() {
        return (!isStatic() && !isFinal() && !Modifier.isPrivate(this.method.getModifiers()));
    }

    @Override
    public boolean isAnnotated(String annotationName) {
        return annotationAttributesMap.containsKey(annotationName);
    }

    @Override
    public AnnotationAttributes getAnnotationAttributes(String annotationName) {
        return annotationAttributesMap.get(annotationName);
    }

    @Override
    public AnnotationAttributes getAnnotationAttributes(String annotationName, boolean classValuesAsString) {
        return null;
    }

    @Override
    public AnnotationAttributes getAnnotationAttributes(Class<? extends Annotation> type) {
        return getAnnotationAttributes(type.getName());
    }

    @Override
    public Map<String, AnnotationAttributes> getAnnotationAttributesAll(Class<? extends Annotation> type) {
        return AnnotationUtils.getAnnotationAttributesAll(type, annotationTree, annotationAttributesMap);
    }

    @Override
    public boolean hasMetaAnnotation(String annotationName) {
        return false;
    }

    @Override
    public boolean hasAnnotation(String metaAnnotationName) {
        return false;
    }
}
