package com.chy.summer.framework.aop.aopalliance.intercept;

/**
 * 调用方法的拦截器
 * 这个方法调用的可能是连接点，同时也可能是另一个拦截器
 */
public interface MethodInterceptor extends Interceptor {
	
	/**
	 * 引用连接点方法
	 * 实现此方法以在调用之前和之后执行额外的处理
	 */
	Object invoke(MethodInvocation invocation) throws Throwable;
}
