package com.chy.summer.framework.annotation.stereotype;

import com.chy.summer.framework.annotation.core.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Service {

    @AliasFor(annotation = Component.class)
    String value() default "";

}
