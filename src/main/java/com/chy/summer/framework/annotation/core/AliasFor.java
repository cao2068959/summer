package com.chy.summer.framework.annotation.core;

import java.lang.annotation.Annotation;

public @interface AliasFor {

    @AliasFor("attribute")
    String value() default "";

    @AliasFor("value")
    String attribute() default "";

    Class<? extends Annotation> annotation() default Annotation.class;
}
