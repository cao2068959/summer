package com.chy.summer.framework.annotation.core;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface AliasFor {

    @AliasFor("name")
    String value() default "";

    /**
     *  目标属性的名字是什么,如果不写默认就是这个注解下面方法的名字
     */
    @AliasFor("value")
    String name() default "";

    /**
     *  目标注解的类
     */
    Class<? extends Annotation> annotation() default Annotation.class;
}
