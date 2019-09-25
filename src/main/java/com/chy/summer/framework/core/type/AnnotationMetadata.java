package com.chy.summer.framework.core.type;

import com.chy.summer.framework.core.annotation.AnnotationAttributes;

import java.lang.annotation.Annotation;

public interface AnnotationMetadata {

    /**
     * 判断是否有某一个注解,这里会把 派生注解和继承注解也算进去
     */
    boolean hasMetaAnnotation(String annotationName);

    /**
     * 判断是否有某一个注解,这里只会判断 当前类上面的注解
     */
    boolean hasAnnotation(String metaAnnotationName);

    String getClassName();

    /**
     * 判断是不是一个封闭的内部类
     */
    boolean isIndependent();

    boolean isConcrete();

    boolean isAbstract();

    /**
     * 获取 类中指定注解类型的 所有属性
     * @param type
     * @return
     */
    AnnotationAttributes getAnnotationAttributes(Class<? extends Annotation> type);
}
