package com.chy.summer.framework.util;

import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class AnnotatedElementUtils {


    /**
     *  检查 annotatedElement 上面有没有指定 类型的注解,如果有的话 把他生成 AnnotationAttributes
     * @param annotatedElement
     * @param type
     * @return
     */
    public static AnnotationAttributes getMergedAnnotationAttributes(AnnotatedElement annotatedElement,
                                                                     Class<? extends Annotation> type) {
        Annotation annoation = getAnnoationByType(annotatedElement, type);
        if(annoation == null){return null;}
        return AnnotationUtils.pareAnnotationToAttributes(annoation,type).getAnnotationAttributes();
    }

    /**
     * 判断 对应 类/属性 上面是否存在指定的 注解，存在的话返回
     * @return
     */
    private static Annotation getAnnoationByType(AnnotatedElement annotatedElement,Class<? extends Annotation> type){
        return annotatedElement.getAnnotation(type);
    }


    /**
     * 判断 方法 类上有没有某个注解
     * @param element
     * @param annotationType
     * @return
     */
    public static boolean hasAnnotation(AnnotatedElement element, Class<? extends Annotation> annotationType) {
        if (element.isAnnotationPresent(annotationType)) {
            return true;
        }
        return false;
    }


    public static boolean hasAnnotation(AnnotatedElement element, String annotationType) {
        return Arrays.stream(element.getDeclaredAnnotations())
                .anyMatch(annotation -> annotation.annotationType().getName().equals(annotationType));

    }


    public static AnnotatedElement forAnnotations(final Annotation... annotations) {
        return new AnnotatedElement() {
            @Override
            @SuppressWarnings("unchecked")
            public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
                for (Annotation ann : annotations) {
                    if (ann.annotationType() == annotationClass) {
                        return (T) ann;
                    }
                }
                return null;
            }
            @Override
            public Annotation[] getAnnotations() {
                return annotations;
            }
            @Override
            public Annotation[] getDeclaredAnnotations() {
                return annotations;
            }
        };
    }
}
