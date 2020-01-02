package com.chy.summer.framework.core.type;

import com.chy.summer.framework.core.annotation.AnnotationAttributeHolder;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.sun.istack.internal.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 *  AnnotationMetadata 的默认实现类
 *  因为 一个Metadata 可能会同时需要继承多个抽象类/类(不但有注解的能力,还有一个普通类应有的能力), JAVA 不支持多继承
 *  所以这里采用了 jdk8 使用 default 接口的方式
 *
 *  如果使用 DefaultAnnotationMetadata 需要实现 2个数据提供的接口 getOwnAllAnnotatedType, getOwnAllAnnotated
 */
public interface DefaultAnnotationMetadata extends AnnotationMetadata {


    /**
     * 拥有的所有注解的类型
     * @return
     */
    Set<String> getOwnAllAnnotatedType();


    /**
     * 获取拥有的所有的注解
     * @return
     */
    Map<String, AnnotationAttributeHolder> getOwnAllAnnotated();


    /**
     * 获取所有注解的类型
     * @return
     *
     *
     */
    @Override
    default Set<String> getAnnotationTypes(){
         return getOwnAllAnnotatedType();
     }


    @Override
    @Nullable
    default AnnotationAttributes getAnnotationAttributes(String annotationName){
        if(!hasMetaAnnotation(annotationName)){
            return null;
        }

        Map<String, AnnotationAttributeHolder> ownAllAnnotated = getOwnAllAnnotated();

        if (ownAllAnnotated.containsKey(annotationName)) {
            return ownAllAnnotated.get(annotationName).getAnnotationAttributes();
        }

        for (AnnotationAttributeHolder holder : ownAllAnnotated.values()) {
            if (holder.getContain().contains(annotationName)) {
                List<AnnotationAttributeHolder> childAnntationHolder = holder.getChildAnntationHolder(annotationName);
                if (childAnntationHolder.size() > 0) {
                    return childAnntationHolder.get(1).getAnnotationAttributes();
                }
            }
        }
        return null;
    }


    @Nullable
    AnnotationAttributes getAnnotationAttributes(String annotationName, boolean classValuesAsString);


    /**
     * 获取 类、方法 中指定注解类型的 所有属性
     * @param type
     * @return
     */
    default AnnotationAttributes getAnnotationAttributes(Class<? extends Annotation> type){
        String annotationName = type.getName();
        return getAnnotationAttributes(annotationName);
    }


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
