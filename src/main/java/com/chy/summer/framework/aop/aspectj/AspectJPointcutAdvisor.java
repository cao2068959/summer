package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.aop.Pointcut;
import com.chy.summer.framework.aop.PointcutAdvisor;
import com.chy.summer.framework.aop.aopalliance.Advice;
import com.chy.summer.framework.core.Ordered;
import com.chy.summer.framework.util.Assert;
import com.sun.istack.internal.Nullable;

/**
 * 封装了AspectJ通知和切入点
 */
public class AspectJPointcutAdvisor implements PointcutAdvisor, Ordered {

	private final AbstractAspectJAdvice advice;

	private final Pointcut pointcut;

	@Nullable
	private Integer order;


	/**
	 * 为给定Aspect创建一个新的AspectJPointcutAdvisor
	 */
	public AspectJPointcutAdvisor(AbstractAspectJAdvice advice) {
		Assert.notNull(advice, "Advice不可为空");
		this.advice = advice;
		this.pointcut = advice.buildSafePointcut();
	}

	/**
	 * 设置执行优先度
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public boolean isPerInstance() {
		return true;
	}

	/**
	 * 获取通知
	 */
	@Override
	public Advice getAdvice() {
		return this.advice;
	}

	/**
	 * 获取切入点
	 */
	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}

	/**
	 * 获取执行优先级
	 */
	@Override
	public int getOrder() {
		if (this.order != null) {
			return this.order;
		}
		else {
			return this.advice.getOrder();
		}
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AspectJPointcutAdvisor)) {
			return false;
		}
		AspectJPointcutAdvisor otherAdvisor = (AspectJPointcutAdvisor) other;
		return this.advice.equals(otherAdvisor.advice);
	}

	@Override
	public int hashCode() {
		return AspectJPointcutAdvisor.class.hashCode() * 29 + this.advice.hashCode();
	}

}