package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.aop.MethodMatcher;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;

/**
 * 将MethodInterceptor实例与MethodMatcher组合在一起，以用作advisor链中的元素。
 */
class InterceptorAndDynamicMethodMatcher {
	/**
	 * 方法拦截器
	 */
	final MethodInterceptor interceptor;

	/**
	 * 方法匹配器
	 */
	final MethodMatcher methodMatcher;

	public InterceptorAndDynamicMethodMatcher(MethodInterceptor interceptor, MethodMatcher methodMatcher) {
		this.interceptor = interceptor;
		this.methodMatcher = methodMatcher;
	}

}