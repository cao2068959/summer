package com.chy.summer.framework.core.type;

import com.sun.istack.internal.Nullable;

import java.util.Map;

public interface AnnotatedTypeMetadata {


    boolean isAnnotated(String annotationName);


    @Nullable
    Map<String, Object> getAnnotationAttributes(String annotationName);


    @Nullable
    Map<String, Object> getAnnotationAttributes(String annotationName, boolean classValuesAsString);


    @Nullable
    Map<String, Object> getAllAnnotationAttributes(String annotationName);


    @Nullable
    Map<String, Object> getAllAnnotationAttributes(String annotationName, boolean classValuesAsString);
}
