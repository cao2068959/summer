package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.annotation.core.AliasFor;
import com.chy.summer.framework.context.annotation.constant.Autowire;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {


    @AliasFor("name")
    String[] value() default {};


    @AliasFor("value")
    String[] name() default {};


    Autowire autowire() default Autowire.NO;


    String initMethod() default "";


    String destroyMethod() default "";

}