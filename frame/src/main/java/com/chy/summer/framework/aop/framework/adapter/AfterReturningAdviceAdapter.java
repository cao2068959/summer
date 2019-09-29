package com.chy.summer.framework.aop.framework.adapter;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.AfterReturningAdvice;
import com.chy.summer.framework.aop.aopalliance.Advice;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;

import java.io.Serializable;

/**
 * AfterReturningAdvice的适配器
 */
class AfterReturningAdviceAdapter implements AdvisorAdapter, Serializable {

	/**
	 * 此适配器是否解析这个advice对象？
	 */
	@Override
	public boolean supportsAdvice(Advice advice) {
		return (advice instanceof AfterReturningAdvice);
	}

	/**
	 * 返回一个advisor对应的拦截器
	 */
	@Override
	public MethodInterceptor getInterceptor(Advisor advisor) {
		AfterReturningAdvice advice = (AfterReturningAdvice) advisor.getAdvice();
		return new AfterReturningAdviceInterceptor(advice);
	}

}
