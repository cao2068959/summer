package com.chy.summer.framework.aop.support;

import com.chy.summer.framework.aop.Pointcut;
import com.chy.summer.framework.aop.aopalliance.Advice;
import com.sun.istack.internal.Nullable;

import java.io.Serializable;

/**
 * 默认的切入点Advisor的实现,也是最常见的实现
 * 除了introductions之外，它可以和任意的切入点或者是通知一起使用。
 */
public class DefaultPointcutAdvisor extends AbstractGenericPointcutAdvisor implements Serializable {

	/**
	 * 切入点对象
	 * 默认的时候可以匹配所有的方法
	 */
	private Pointcut pointcut = Pointcut.TRUE;


	/**
	 * 创建一个空的DefaultPointcutAdvisor
	 * 使用之前必须先设置Advice
	 */
	public DefaultPointcutAdvisor() {
	}

	/**
	 * 创建一个匹配所有方法的Advice的Advisor
	 */
	public DefaultPointcutAdvisor(Advice advice) {
		this(Pointcut.TRUE, advice);
	}

	/**
	 * 创建一个, 同时设置Pointcut和Advice.
	 */
	public DefaultPointcutAdvisor(Pointcut pointcut, Advice advice) {
		this.pointcut = pointcut;
		setAdvice(advice);
	}


	/**
	 * 设置切入点
	 */
	public void setPointcut(@Nullable Pointcut pointcut) {
		this.pointcut = (pointcut != null ? pointcut : Pointcut.TRUE);
	}

	/**
	 * 获取切入点
	 */
	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}


	@Override
	public String toString() {
		return getClass().getName() + ": pointcut [" + getPointcut() + "]; advice [" + getAdvice() + "]";
	}

}