package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.annotation.core.AliasFor;
import com.chy.summer.framework.annotation.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Configuration {

    @AliasFor(annotation = Component.class)
    String value() default "";

}
