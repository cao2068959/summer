package com.chy.summer.framework.annotation.constant;


import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Set;

public class AnnotationConstant {

    public static final Set<Class> ignoreAnnotation;

    static {
        ignoreAnnotation = new HashSet<Class>();
        ignoreAnnotation.add(Target.class);
        ignoreAnnotation.add(Retention.class);
        ignoreAnnotation.add(Documented.class);
    }


}
