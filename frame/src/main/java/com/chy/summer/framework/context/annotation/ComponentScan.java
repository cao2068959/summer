package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.annotation.core.AliasFor;
import com.chy.summer.framework.beans.support.BeanNameGenerator;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ComponentScan {

    @AliasFor("basePackages")
    String[] value() default {};

    @AliasFor("value")
    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    /**
     * 设置 beanName 生成器,如果不自定义则就是 AnnotationBeanNameGenerator
     * @return
     */
    Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

}