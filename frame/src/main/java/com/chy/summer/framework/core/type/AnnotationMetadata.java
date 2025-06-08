package com.chy.summer.framework.core.type;

import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import javax.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Annotation 类型的 元数据 ,因为元数据本来就是代表 类 所以继承了ClassMetadata
 * 同时也有一些注解才有的特性,所以继承了 AnnotationBehavior
 */
public interface AnnotationMetadata extends ClassMetadata, AnnotationBehavior {

    /**
     * 获取打了某个注解的所有方法
     * @param name
     * @return
     */
    Set<MethodMetadata> getAnnotatedMethods(String name);


    /**
     * 有没有 某个方法打了指定的注解
     * @param name
     * @return
     */
    boolean hasAnnotatedMethods(String name);

}
