package com.chy.summer.framework.core.type;

import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import javax.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

/**
 *
 *  抽象出来的 注解 行为接口
 *  代表了 metaData(类/方法/属性) 具有可以标注注解的能力
 *
 */
public interface AnnotationBehavior {

     Set<String> getAnnotationTypes();

    /**
     * 查出这个 元数据(类,方法,属性) 上指定注解的 属性对象,如果不存在对应的注解,就返回Null
     * @param annotationName
     * @return
     */
    @Nullable
    AnnotationAttributes getAnnotationAttributes(String annotationName);



    /**
     * 同上面 getAnnotationAttributes(string) 只是这边入参是 注解的类型
     * @param type
     * @return
     */
    AnnotationAttributes getAnnotationAttributes(Class<? extends Annotation> type);


    /**
     * 获取 类、方法 中指定注解类型的 所有属性，这个方法可以拿包括是继承的注解
     * 比如： 入参是 @A 注解，在一个类上有 @B , @C 注解 这2个注解 里面都标注上了 @A 注解，那么就会返回 @B,@C 上标准的 @A 注解对象
     * 一般用在 比如 @Conditional 或者 @Import 这种需要收集所有注解属性的地方
     *
     */
    List<AnnotationAttributes> getAnnotationAttributesAll(Class<? extends Annotation> type);


    /**
     * 判断是否有某一个注解,这里会把 派生注解和继承注解也算进去
     */
    boolean hasMetaAnnotation(String annotationName);

    /**
     * 判断是否有某一个注解,这里只会判断 当前类上面的注解
     */
    boolean hasAnnotation(String annotationName);
}
