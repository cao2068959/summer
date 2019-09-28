package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.context.annotation.constant.ScopeType;
import com.chy.summer.framework.context.annotation.constant.ScopedProxyMode;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scope {

    ScopeType value() default ScopeType.SINGLETON;

    ScopedProxyMode proxyMode() default ScopedProxyMode.DEFAULT;

}
