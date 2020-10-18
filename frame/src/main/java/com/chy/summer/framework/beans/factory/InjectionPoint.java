package com.chy.summer.framework.beans.factory;

import com.chy.summer.framework.core.MethodParameter;
import com.chy.summer.framework.util.Assert;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class InjectionPoint {

    @Getter
    protected MethodParameter methodParameter;

    protected Field field;

    private volatile Annotation[] fieldAnnotations;

    public InjectionPoint(Field field) {
        this.field = field;
    }

    public InjectionPoint(MethodParameter methodParameter) {
        this.methodParameter = methodParameter;
    }

    protected final MethodParameter obtainMethodParameter() {
        Assert.state(this.methodParameter != null, "Neither Field nor MethodParameter");
        return this.methodParameter;
    }

}
