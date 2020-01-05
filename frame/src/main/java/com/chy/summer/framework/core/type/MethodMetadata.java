package com.chy.summer.framework.core.type;

public interface MethodMetadata extends AnnotationBehavior {


    String getMethodName();


    String getDeclaringClassName();


    String getReturnTypeName();


    boolean isAbstract();


    boolean isStatic();


    boolean isFinal();


    boolean isOverridable();

}