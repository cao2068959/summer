package com.chy.summer.framework.core;

import com.chy.summer.framework.util.Assert;
import com.sun.istack.internal.Nullable;
import lombok.Data;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Map;

@Data
public class MethodParameter {

    private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

    @Nullable
    private volatile Parameter parameter;

    private int nestingLevel = 1;

    private final Executable executable;

    private final int parameterIndex;

    @Nullable
    Map<Integer, Integer> typeIndexesPerLevel;

    @Nullable
    private volatile Class<?> containingClass;

    @Nullable
    private volatile Class<?> parameterType;

    @Nullable
    private volatile Type genericParameterType;

    @Nullable
    @Getter
    private volatile Annotation[] parameterAnnotations;

    @Nullable
    private volatile ParameterNameDiscoverer parameterNameDiscoverer;

    @Nullable
    private volatile String parameterName;

    @Nullable
    private volatile MethodParameter nestedMethodParameter;


    public MethodParameter(Method method, int parameterIndex) {
        this(method, parameterIndex, 1);
    }


    public MethodParameter(Method method, int parameterIndex, int nestingLevel) {
        Assert.notNull(method, "Method must not be null");
        this.executable = method;
        this.parameterIndex = validateIndex(method, parameterIndex);
        this.nestingLevel = nestingLevel;
    }

    public Class<?> getParameterType() {
        Class<?> paramType = this.parameterType;
        if (paramType == null) {
            if (this.parameterIndex < 0) {
                Method method = getMethod();
                paramType = (method != null ? method.getReturnType() : void.class);
            }
            else {
                paramType = this.executable.getParameterTypes()[this.parameterIndex];
            }
            this.parameterType = paramType;
        }
        return paramType;
    }


    public MethodParameter(Constructor<?> constructor, int parameterIndex) {
        this(constructor, parameterIndex, 1);
    }


    public MethodParameter(Constructor<?> constructor, int parameterIndex, int nestingLevel) {
        Assert.notNull(constructor, "Constructor must not be null");
        this.executable = constructor;
        this.parameterIndex = validateIndex(constructor, parameterIndex);
        this.nestingLevel = nestingLevel;
    }


    public MethodParameter(MethodParameter original) {
        Assert.notNull(original, "Original must not be null");
        this.executable = original.executable;
        this.parameterIndex = original.parameterIndex;
        this.parameter = original.parameter;
        this.nestingLevel = original.nestingLevel;
        this.typeIndexesPerLevel = original.typeIndexesPerLevel;
        this.containingClass = original.containingClass;
        this.parameterType = original.parameterType;
        this.genericParameterType = original.genericParameterType;
        this.parameterAnnotations = original.parameterAnnotations;
        this.parameterNameDiscoverer = original.parameterNameDiscoverer;
        this.parameterName = original.parameterName;
    }

    private static int validateIndex(Executable executable, int parameterIndex) {
        int count = executable.getParameterCount();
        Assert.isTrue(parameterIndex < count, () -> "Parameter index needs to be between -1 and " + (count - 1));
        return parameterIndex;
    }

    public Method getMethod() {
        return (this.executable instanceof Method ? (Method) this.executable : null);
    }

    public Class<?> getDeclaringClass() {
        return this.executable.getDeclaringClass();
    }

    public static MethodParameter forExecutable(Executable executable, int parameterIndex) {
        if (executable instanceof Method) {
            return new MethodParameter((Method) executable, parameterIndex);
        }
        else if (executable instanceof Constructor) {
            return new MethodParameter((Constructor<?>) executable, parameterIndex);
        }
        else {
            throw new IllegalArgumentException("Not a Method/Constructor: " + executable);
        }
    }

}
