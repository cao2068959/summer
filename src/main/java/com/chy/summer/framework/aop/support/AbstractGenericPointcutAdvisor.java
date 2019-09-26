package com.chy.summer.framework.aop.support;

import com.chy.summer.framework.aop.aopalliance.Advice;

/**
 * 抽象的通用切入点Advisor
 */
public abstract class AbstractGenericPointcutAdvisor extends AbstractPointcutAdvisor {

	/**
	 * Advisor管理的通知
	 */
	private Advice advice = EMPTY_ADVICE;


	/**
	 * 设置这个Advisor管理的通知
	 */
	public void setAdvice(Advice advice) {
		this.advice = advice;
	}

	/**
	 * 获取通知
	 */
	@Override
	public Advice getAdvice() {
		return this.advice;
	}


	@Override
	public String toString() {
		return getClass().getName() + ": advice [" + getAdvice() + "]";
	}

}