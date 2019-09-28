package com.chy.summer.framework.aop.aopalliance.intercept;

/**
 * 调用构造器的拦截器
 * 这个方法调用的可能是构造器，同时也可能是另一个拦截器
 */
public interface ConstructorInterceptor extends Interceptor  {

	/**
	 * 引用构造器
	 * 实现此方法以在调用之前和之后执行额外的处理
	 */
	Object construct(ConstructorInvocation invocation) throws Throwable;
}