package com.chy.summer.framework.aop.support;

import com.chy.summer.framework.aop.ClassFilter;
import com.chy.summer.framework.aop.DynamicIntroductionAdvice;
import com.chy.summer.framework.aop.IntroductionAdvisor;
import com.chy.summer.framework.aop.IntroductionInfo;
import com.chy.summer.framework.aop.aopalliance.Advice;
import com.chy.summer.framework.core.Ordered;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.sun.istack.internal.Nullable;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 简单的IntroductionAdvisor实现，默认情况下适用于任何类。
 */
public class DefaultIntroductionAdvisor implements IntroductionAdvisor, ClassFilter, Ordered, Serializable {

	/**
	 * 持有的通知
	 */
	private final Advice advice;

	/**
	 * 需要实现的接口列表
	 */
	private final Set<Class<?>> interfaces = new LinkedHashSet<>();

	/**
	 * 调用优先级
	 */
	private int order = Integer.MAX_VALUE;


	/**
	 * 为给定advice创建一个DefaultIntroductionAdvisor。
	 */
	public DefaultIntroductionAdvisor(Advice advice) {
		this(advice, (advice instanceof IntroductionInfo ? (IntroductionInfo) advice : null));
	}

	/**
	 * 为给定advice创建一个DefaultIntroductionAdvisor
	 */
	public DefaultIntroductionAdvisor(Advice advice, @Nullable IntroductionInfo introductionInfo) {
		Assert.notNull(advice, "Advice不可为空");
		this.advice = advice;
		if (introductionInfo != null) {
			Class<?>[] introducedInterfaces = introductionInfo.getInterfaces();
			if (introducedInterfaces.length == 0) {
				throw new IllegalArgumentException("IntroductionAdviceSupport没有实现任何接口");
			}
			for (Class<?> ifc : introducedInterfaces) {
				//将接口添加到接口列表中
				addInterface(ifc);
			}
		}
	}

	/**
	 * 为给定advice创建一个DefaultIntroductionAdvisor，并添加指定的接口
	 */
	public DefaultIntroductionAdvisor(DynamicIntroductionAdvice advice, Class<?> intf) {
		Assert.notNull(advice, "Advice不可为空");
		this.advice = advice;
		addInterface(intf);
	}


	/**
	 * 将指定的接口添加到要引入的接口列表中
	 */
	public void addInterface(Class<?> intf) {
		Assert.notNull(intf, "Interface不可为空");
		if (!intf.isInterface()) {
			throw new IllegalArgumentException("指定的类[" + intf.getName() + "]必须是接口");
		}
		this.interfaces.add(intf);
	}

	/**
	 * 获取此Advisor或者Advice引入的其他接口。
	 */
	@Override
	public Class<?>[] getInterfaces() {
		return this.interfaces.toArray(new Class<?>[this.interfaces.size()]);
	}

	/**
	 * 判断Advisor能否通过IntroductionAdvisor来实现
	 */
	@Override
	public void validateInterfaces() throws IllegalArgumentException {
		for (Class<?> ifc : this.interfaces) {
			if (this.advice instanceof DynamicIntroductionAdvice &&
					!((DynamicIntroductionAdvice) this.advice).implementsInterface(ifc)) {
			 throw new IllegalArgumentException("DynamicIntroductionAdvice [" + this.advice + "] " +
					 "未实现指定用于引入的接口[" + ifc.getName() + "]");
			}
		}
	}


	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * 获取此对象的顺序值
	 * 值越小优先度越高，相同的顺序值将导致受影响对象的任意排序位置
	 */
	@Override
	public int getOrder() {
		return this.order;
	}

	/**
	 * 获取通知
	 */
	@Override
	public Advice getAdvice() {
		return this.advice;
	}

	@Override
	public boolean isPerInstance() {
		return true;
	}

	/**
	 * 获取类过滤器，确定此Introduction应适用于哪些目标类。
	 */
	@Override
	public ClassFilter getClassFilter() {
		return this;
	}

	/**
	 * 切入点应该应用于给定的接口/类
	 * @param clazz clazz候选目标类型
	 */
	@Override
	public boolean matches(Class<?> clazz) {
		return true;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof DefaultIntroductionAdvisor)) {
			return false;
		}
		DefaultIntroductionAdvisor otherAdvisor = (DefaultIntroductionAdvisor) other;
		return (this.advice.equals(otherAdvisor.advice) && this.interfaces.equals(otherAdvisor.interfaces));
	}

	@Override
	public int hashCode() {
		return this.advice.hashCode() * 13 + this.interfaces.hashCode();
	}

	@Override
	public String toString() {
		return ClassUtils.getShortName(getClass()) + ": advice [" + this.advice + "]; interfaces " +
				ClassUtils.classNamesToString(this.interfaces);
	}

}