package com.chy.summer.framework.core.type;

import com.chy.summer.framework.core.annotation.AnnotationAttributes;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface AnnotationMetadata extends ClassMetadata,AnnotatedTypeMetadata {


    String getClassName();

    /**
     * 判断是不是一个封闭的内部类
     */
    boolean isIndependent();

    boolean isConcrete();

    boolean isAbstract();

    boolean isInterface();

    boolean hasAnnotatedMethods(String name);


    Set<String> getAnnotationTypes();

    /**
     * 获取打了某个注解的所有方法
     * @param name
     * @return
     */
    Set<MethodMetadata> getAnnotatedMethods(String name);
}
