package com.chy.summer.framework.core.type;

import com.chy.summer.framework.core.annotation.AnnotationAttributeHolder;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.annotation.AnnotationUtils;
import com.chy.summer.framework.util.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class StandardAnnotationMetadata extends StandardClassMetadata implements AnnotationMetadata {

    private final Annotation[] annotations;

    private final boolean nestedAnnotationsAsMap;

    //注解的继承关系
    private final Map<String, AnnotationAttributeHolder> annotationAttributes;

    //拥有的全部的注解的类型
    private final Set<String> annotationType = new HashSet<>();


    public StandardAnnotationMetadata(Class<?> introspectedClass, boolean nestedAnnotationsAsMap) {
        super(introspectedClass);
        this.annotations = introspectedClass.getAnnotations();
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
        //解析类上的注解
        annotationAttributes = AnnotationUtils.getAnnotationInfoByClass(introspectedClass, true);

        annotationAttributes.values().stream().forEach(holder -> {
            annotationType.add(holder.getName());
            annotationType.addAll(holder.getContain());
        });


    }

    @Override
    public boolean hasMetaAnnotation(String annotationName) {
        return annotationType.contains(annotationName);
    }

    @Override
    public boolean hasAnnotation(String metaAnnotationName) {
        return false;
    }


    @Override
    public AnnotationAttributes getAnnotationAttributes(Class<? extends Annotation> type) {
        String annotationName = type.getName();
        if(!hasMetaAnnotation(annotationName)){
            return null;
        }

        if (annotationAttributes.containsKey(annotationName)) {
            return annotationAttributes.get(annotationName).getAnnotationAttributes();
        }
        for (AnnotationAttributeHolder holder : annotationAttributes.values()) {

            if (holder.getContain().contains(annotationName)) {
                List<AnnotationAttributeHolder> childAnntationHolder = holder.getChildAnntationHolder(annotationName);
                if (childAnntationHolder.size() > 0) {
                    return childAnntationHolder.get(1).getAnnotationAttributes();
                }
            }
        }
        return null;
    }

    @Override
    public Map<String, AnnotationAttributes> getAnnotationAttributesAll(Class<? extends Annotation> type) {
        return null;
    }

    @Override
    public boolean hasAnnotatedMethods(String name) {
        return false;
    }

    @Override
    public Set<String> getAnnotationTypes() {
        return annotationType;
    }

    /**
     * 这里是用反射直接获取的
     *
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
        } catch (Throwable ex) {
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
