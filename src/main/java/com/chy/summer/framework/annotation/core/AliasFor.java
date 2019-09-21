package com.chy.summer.framework.annotation.core;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface AliasFor {

    /**
     *  目标属性的名字是什么,如果不写默认就是这个注解下面方法的名字
     */
    String name() default "";

    /**
     *  目标注解的类
     */
    Class<? extends Annotation> annotation();
}
