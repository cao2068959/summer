package com.chy.summer.framework.annotation.stereotype;

import com.chy.summer.framework.annotation.core.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Repository
{
    @AliasFor(annotation = Component.class)
    String value() default "";

}
