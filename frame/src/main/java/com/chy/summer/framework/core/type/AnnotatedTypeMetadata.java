package com.chy.summer.framework.core.type;

import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.sun.istack.internal.Nullable;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface AnnotatedTypeMetadata {


    boolean isAnnotated(String annotationName);


    @Nullable
    AnnotationAttributes getAnnotationAttributes(String annotationName);


    @Nullable
    AnnotationAttributes getAnnotationAttributes(String annotationName, boolean classValuesAsString);


    /**
     * 获取 类中指定注解类型的 所有属性
     * @param type
     * @return
     */
    AnnotationAttributes getAnnotationAttributes(Class<? extends Annotation> type);


    /**
     * 判断是否有某一个注解,这里会把 派生注解和继承注解也算进去
     */
    boolean hasMetaAnnotation(String annotationName);

    /**
     * 判断是否有某一个注解,这里只会判断 当前类上面的注解
     */
    boolean hasAnnotation(String metaAnnotationName);

}
