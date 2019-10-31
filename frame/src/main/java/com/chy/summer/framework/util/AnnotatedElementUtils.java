package com.chy.summer.framework.util;

import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
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


    }


    private static <T> T searchWithGetSemantics(AnnotatedElement element,
                                                Class<? extends Annotation> annotationType, String annotationName,
                                                Class<? extends Annotation> containerType, Processor<T> processor,
                                                Set<AnnotatedElement> visited, int metaDepth) {

        if (visited.add(element)) {
            try {
                // Start searching within locally declared annotations
                List<Annotation> declaredAnnotations = Arrays.asList(element.getDeclaredAnnotations());
                T result = searchWithGetSemanticsInAnnotations(element, declaredAnnotations,
                        annotationType, annotationName, containerType, processor, visited, metaDepth);
                if (result != null) {
                    return result;
                }
                AnnotationUtils

                if (element instanceof Class) {  // otherwise getAnnotations doesn't return anything new
                    Class<?> superclass = ((Class) element).getSuperclass();
                    if (superclass != null && superclass != Object.class) {
                        List<Annotation> inheritedAnnotations = new LinkedList<>();
                        for (Annotation annotation : element.getAnnotations()) {
                            if (!declaredAnnotations.contains(annotation)) {
                                inheritedAnnotations.add(annotation);
                            }
                        }
                        // Continue searching within inherited annotations
                        result = searchWithGetSemanticsInAnnotations(element, inheritedAnnotations,
                                annotationType, annotationName, containerType, processor, visited, metaDepth);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
            catch (Throwable ex) {
                AnnotationUtils.handleIntrospectionFailure(element, ex);
            }
        }

        return null;
    }


}
