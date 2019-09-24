package com.chy.summer.framework.core.type;

public interface AnnotationMetadata {

    /**
     * 判断是否有某一个注解,这里会把 派生注解和继承注解也算进去
     */
    boolean hasMetaAnnotation(String annotationName);

    /**
     * 判断是否有某一个注解,这里只会判断 当前类上面的注解
     */
    boolean hasAnnotation(String metaAnnotationName);

}
