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
     * 获取 类、方法 中指定注解类型的 所有属性
     * @param type
     * @return
     */
    AnnotationAttributes getAnnotationAttributes(Class<? extends Annotation> type);


    /**
     * 获取 类、方法 中指定注解类型的 所有属性，这个方法可以拿包括是派生的注解
     * 比如： 入参是 @A 注解，在一个类上有 @B , @C 注解 这2个注解 里面都标注上了 @A 注解，那么就会返回 @B,@C 注解的对应属性
     *
     * @return key: 真正类上注解的全路径，以上面例子就是 B,C 注解的全路径 , value 对应的注解的值
     */
    Map<String,AnnotationAttributes> getAnnotationAttributesAll(Class<? extends Annotation> type);


    /**
     * 判断是否有某一个注解,这里会把 派生注解和继承注解也算进去
     */
    boolean hasMetaAnnotation(String annotationName);

    /**
     * 判断是否有某一个注解,这里只会判断 当前类上面的注解
     */
    boolean hasAnnotation(String metaAnnotationName);

}
