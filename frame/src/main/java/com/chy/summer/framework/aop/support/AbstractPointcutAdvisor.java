package com.chy.summer.framework.aop.support;

import com.chy.summer.framework.aop.PointcutAdvisor;
import com.chy.summer.framework.aop.aopalliance.Advice;
import com.chy.summer.framework.core.ordered.Ordered;
import com.chy.summer.framework.util.ObjectUtils;
import javax.annotation.Nullable;

import java.io.Serializable;

/**
 * PointcutAdvisor的实现抽象类
 * 主要用于返回和自由配置特定的pointcut/advice。
 */
public abstract class AbstractPointcutAdvisor implements PointcutAdvisor, Ordered, Serializable {

	/**
	 * 优先级顺序
	 */
	@Nullable
	private Integer order;


	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * 获取执行的优先级
	 */
	@Override
	public int getOrder() {
		if (this.order != null) {
			return this.order;
		}
		//获取通知
		Advice advice = getAdvice();
		if (advice instanceof Ordered) {
			//获取通知的执行优先级
			return ((Ordered) advice).getOrder();
		}
		//优先级最低
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public boolean isPerInstance() {
		return true;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PointcutAdvisor)) {
			return false;
		}
		PointcutAdvisor otherAdvisor = (PointcutAdvisor) other;
		return (ObjectUtils.nullSafeEquals(getAdvice(), otherAdvisor.getAdvice()) &&
				ObjectUtils.nullSafeEquals(getPointcut(), otherAdvisor.getPointcut()));
	}

	@Override
	public int hashCode() {
		return PointcutAdvisor.class.hashCode();
	}

}