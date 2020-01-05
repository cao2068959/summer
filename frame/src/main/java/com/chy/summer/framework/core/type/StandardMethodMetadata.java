package com.chy.summer.framework.core.type;

import com.chy.summer.framework.core.annotation.AnnotationAttributeHolder;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.annotation.AnnotationUtils;
import com.chy.summer.framework.util.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class StandardMethodMetadata implements MethodMetadata, DefaultAnnotationBehavior {

    private final boolean nestedAnnotationsAsMap;
    private Method method;

    //注解的继承关系
    private final Map<String, AnnotationAttributeHolder> annotationAttributes;

    //拥有的全部的注解的类型
    private final Set<String> annotationType = new HashSet<>();

    public StandardMethodMetadata(Method method, boolean nestedAnnotationsAsMap) {
        this.method = method;
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
        annotationAttributes = AnnotationUtils.getAnnotationInfoByMethod(method);
        annotationAttributes.values().stream().forEach(holder -> {
            annotationType.add(holder.getName());
            annotationType.addAll(holder.getContain());
        });
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
    public Set<String> getOwnAllAnnotatedType() {
        return annotationType;
    }

    @Override
    public Map<String, AnnotationAttributeHolder> getOwnAllAnnotated() {
        return annotationAttributes;
    }
}
