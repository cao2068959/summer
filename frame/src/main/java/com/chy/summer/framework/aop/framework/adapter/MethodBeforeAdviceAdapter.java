package com.chy.summer.framework.aop.framework.adapter;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.MethodBeforeAdvice;
import com.chy.summer.framework.aop.aopalliance.Advice;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;

import java.io.Serializable;

/**
 *  MethodBeforeAdvice的适配器
 */
class MethodBeforeAdviceAdapter implements AdvisorAdapter, Serializable {

	/**
	 * 此适配器是否解析这个advice对象？
	 */
	@Override
	public boolean supportsAdvice(Advice advice) {
		return (advice instanceof MethodBeforeAdvice);
	}

	/**
	 * 返回一个advisor对应的拦截器
	 */
	@Override
	public MethodInterceptor getInterceptor(Advisor advisor) {
		MethodBeforeAdvice advice = (MethodBeforeAdvice) advisor.getAdvice();
		return new MethodBeforeAdviceInterceptor(advice);
	}

}