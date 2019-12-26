package com.chy.summer.framework.core.type;

import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.annotation.AnnotationUtils;
import com.chy.summer.framework.util.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashSet;
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
    public Map<String, AnnotationAttributes> getAnnotationAttributesAll(Class<? extends Annotation> type) {
        return AnnotationUtils.getAnnotationAttributesAll(type,annotationTree,annotationAttributesMap);
    }

    @Override
    public boolean hasAnnotatedMethods(String name) {
        return false;
    }

    @Override
    public Set<String> getAnnotationTypes() {
        return annotationTree.keySet();
    }

    /**
     * 这里是用反射直接获取的
     * @param annotationName
     * @return
     */
    @Override
    public Set<MethodMetadata> getAnnotatedMethods(String annotationName) {
        try {
            Method[] methods = getIntrospectedClass().getDeclaredMethods();
            Set<MethodMetadata> annotatedMethods = new LinkedHashSet<>(4);
            for (Method method : methods) {
                if (!method.isBridge() && method.getAnnotations().length > 0 &&
                        AnnotatedElementUtils.hasAnnotation(method, annotationName)) {
                    annotatedMethods.add(new StandardMethodMetadata(method, this.nestedAnnotationsAsMap));
                }
            }
            return annotatedMethods;
        }
        catch (Throwable ex) {
            throw new IllegalStateException("Failed to introspect annotated methods on " + getIntrospectedClass(), ex);
        }
    }

    @Override
    public boolean isAnnotated(String name) {
        return false;
    }

    @Override
    public AnnotationAttributes getAnnotationAttributes(String annotationName) {
        return null;
    }

    @Override
    public AnnotationAttributes getAnnotationAttributes(String annotationName, boolean classValuesAsString) {
        return null;
    }
}
