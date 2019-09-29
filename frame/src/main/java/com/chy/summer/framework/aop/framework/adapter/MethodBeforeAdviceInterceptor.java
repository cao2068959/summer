package com.chy.summer.framework.aop.framework.adapter;

import com.chy.summer.framework.aop.MethodBeforeAdvice;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.chy.summer.framework.util.Assert;

import java.io.Serializable;

/**
 * MethodBeforeAdvice拦截器,持有MethodBeforeAdvice的实例对象
 */
public class MethodBeforeAdviceInterceptor implements MethodInterceptor, Serializable {

	/**
	 * MethodBeforeAdvice的实例对象
	 */
	private MethodBeforeAdvice advice;


	/**
	 * 设置MethodBeforeAdvice
	 */
	public MethodBeforeAdviceInterceptor(MethodBeforeAdvice advice) {
		Assert.notNull(advice, "Advice不可为空");
		this.advice = advice;
	}

	/**
	 * 执行MethodBeforeAdvice的方法，然后返回链中下一个对象
	 */
	@Override
	public Object invoke(MethodInvocation mi) throws Throwable {
		this.advice.before(mi.getMethod(), mi.getArguments(), mi.getThis() );
		return mi.proceed();
	}

}