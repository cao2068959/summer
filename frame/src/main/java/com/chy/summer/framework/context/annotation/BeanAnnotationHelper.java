package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.util.AnnotatedElementUtils;

import java.lang.reflect.Method;

public class BeanAnnotationHelper {

    public static boolean isBeanAnnotated(Method method) {
        return AnnotatedElementUtils.hasAnnotation(method, Bean.class);
    }

}
