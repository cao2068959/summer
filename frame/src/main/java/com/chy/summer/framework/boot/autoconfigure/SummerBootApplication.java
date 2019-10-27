package com.chy.summer.framework.boot.autoconfigure;

import com.chy.summer.framework.context.annotation.ComponentScan;
import com.chy.summer.framework.context.annotation.Configuration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
@ComponentScan
public @interface SummerBootApplication {
}
