package com.chy.summer.framework.aop;

import com.chy.summer.framework.aop.aopalliance.Advice;

/**
 * aop顾问的根接口
 * 将通知以更为复杂的方式织入到目标对象中，将通知包装为更复杂切面的装配器
 * 用来管理Advice和Pointcut
 * 其中PointcutAdvisor和切点有关，但IntroductionAdvisor和切点无关
 */
public interface Advisor {

	/**
	 * aop通知的容器
	 * {@link #getAdvice()} 如果没有通知，返回一个空的占位符
	 */
	Advice EMPTY_ADVICE = new Advice() {};


	/**
	 * 获取这切面的通知，可以是拦截器，事前通知，抛出通知等
	 */
	Advice getAdvice();


	boolean isPerInstance();

}