package com.chy.summer.framework.context.annotation;

import java.lang.annotation.*;

/**
 * aop启动的注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AspectJAutoProxyRegistrar.class)
public @interface EnableAspectJAutoProxy {

	/**
	 * 是否要创建CGLIB的代理。 默认为false。
	 */
	boolean proxyTargetClass() default false;

	/**
	 * 标识AOP框架应该将代理的ThreadLocal暴露，以便通过AopContext类进行检索
	 * 默认情况下关闭，不能保证AopContext访问将工作
	 */
	boolean exposeProxy() default false;

}