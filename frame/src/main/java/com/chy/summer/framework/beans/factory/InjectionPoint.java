package com.chy.summer.framework.beans.factory;

import com.chy.summer.framework.core.MethodParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class InjectionPoint {

    protected MethodParameter methodParameter;

    protected Field field;

    private volatile Annotation[] fieldAnnotations;

    public InjectionPoint(Field field) {
        this.field = field;
    }

    public InjectionPoint(MethodParameter methodParameter) {
        this.methodParameter = methodParameter;
    }

}
