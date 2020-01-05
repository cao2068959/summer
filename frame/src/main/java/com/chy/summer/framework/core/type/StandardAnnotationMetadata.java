package com.chy.summer.framework.core.type;

import com.chy.summer.framework.core.annotation.AnnotationAttributeHolder;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.annotation.AnnotationUtils;
import com.chy.summer.framework.util.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class StandardAnnotationMetadata extends StandardClassMetadata implements AnnotationMetadata, DefaultAnnotationBehavior {

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
    public Set<String> getOwnAllAnnotatedType() {
        return annotationType;
    }

    @Override
    public Map<String, AnnotationAttributeHolder> getOwnAllAnnotated() {
        return annotationAttributes;
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
    public boolean hasAnnotatedMethods(String name) {
       return !getAnnotatedMethods(name).isEmpty();
    }

}
