package com.chy.summer.framework.boot.autoconfigure;

import com.chy.summer.framework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {


    String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";


    Class<?>[] exclude() default {};

    String[] excludeName() default {};

}