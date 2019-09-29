package com.chy.summer.framework.aop.framework.adapter;

import com.chy.summer.framework.aop.AfterAdvice;
import com.chy.summer.framework.aop.AfterReturningAdvice;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.chy.summer.framework.util.Assert;

import java.io.Serializable;

/**
 * AfterReturningAdvice拦截器,持有AfterReturningAdvice的实例对象
 */
public class AfterReturningAdviceInterceptor implements MethodInterceptor, AfterAdvice, Serializable {

	private final AfterReturningAdvice advice;


	/**
	 * 设置AfterReturningAdvice
	 */
	public AfterReturningAdviceInterceptor(AfterReturningAdvice advice) {
		Assert.notNull(advice, "Advice不可为空");
		this.advice = advice;
	}

	/**
	 * 执行AfterReturningAdvice的方法，然后返回链中下一个对象
	 */
	@Override
	public Object invoke(MethodInvocation mi) throws Throwable {
		Object retVal = mi.proceed();
		this.advice.afterReturning(retVal, mi.getMethod(), mi.getArguments(), mi.getThis());
		return retVal;
	}

}