package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.core.Ordered;
import com.chy.summer.framework.util.Assert;
import com.sun.istack.internal.Nullable;

import java.io.Serializable;

/**
 * 由指定的单例对象支持的AspectInstanceFactory的实现，为每个getAspectInstance（）调用返回相同的实例。
 * 就是SimpleAspectInstanceFactory的单例版
 */
public class SingletonAspectInstanceFactory implements AspectInstanceFactory, Serializable {

	/**
	 * 切面实例
	 */
	private final Object aspectInstance;


	/**
	 * 为给定的切面实例创建一个新的SingletonAspectInstanceFactory。
	 */
	public SingletonAspectInstanceFactory(Object aspectInstance) {
		Assert.notNull(aspectInstance, "Aspect instance不可为空");
		this.aspectInstance = aspectInstance;
	}

	/**
	 * 获取切面实例
	 */
	@Override
	public final Object getAspectInstance() {
		return this.aspectInstance;
	}

	/**
	 * 获取加载器
	 */
	@Override
	@Nullable
	public ClassLoader getAspectClassLoader() {
		return this.aspectInstance.getClass().getClassLoader();
	}

	/**
	 * 获得该工厂的切面实例的顺序
	 * 如果没有设置，这使用备用的顺序
	 */
	@Override
	public int getOrder() {
		if (this.aspectInstance instanceof Ordered) {
			return ((Ordered) this.aspectInstance).getOrder();
		}
		return getOrderForAspectClass(this.aspectInstance.getClass());
	}

	/**
	 * 当没有指定顺序的时候，调用此方法作为后备顺序
	 */
	protected int getOrderForAspectClass(Class<?> aspectClass) {
		return Ordered.LOWEST_PRECEDENCE;
	}

}