package com.chy.summer.framework.aop.target;

import com.chy.summer.framework.aop.TargetSource;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ObjectUtils;

import java.io.Serializable;

/**
 * 拥有给定对象的TargetSource接口的实现
 * AOP框架TargetSource接口的默认实现，通常不需要在应用程序代码中创建此类的对象。
 * 此类是可序列化的，但是SingletonTargetSource的是否可序列化属性将取决于目标是否可序列化。
 */
public class SingletonTargetSource implements TargetSource, Serializable {

	/**
	 * 持有目标对象
	 */
	private final Object target;


	/**
	 * 为给定目标创建一个新的SingletonTargetSource。
	 */
	public SingletonTargetSource(Object target) {
		Assert.notNull(target, "Target对象不可为空");
		this.target = target;
	}

	/**
	 * 获取Target的类型
	 */
	@Override
	public Class<?> getTargetClass() {
		return this.target.getClass();
	}

	/**
	 * 获取Target
	 */
	@Override
	public Object getTarget() {
		return this.target;
	}

	/**
	 * 释放目标
	 * @param target 通过调用getTarget()获得的对象
	 */
	@Override
	public void releaseTarget(Object target) {
		// nothing to do
	}

	/**
	 * 是否每次调用getTarget()都返回相同的对象
	 * 在这种情况下，将不会调用releaseTarget(Object)，并且AOP框架可以对返回值进行缓存。
	 */
	@Override
	public boolean isStatic() {
		return true;
	}


	/**
	 * 如果两个拦截器持有相同的目标，则它们相等。
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SingletonTargetSource)) {
			return false;
		}
		SingletonTargetSource otherTargetSource = (SingletonTargetSource) other;
		return this.target.equals(otherTargetSource.target);
	}

	@Override
	public int hashCode() {
		return this.target.hashCode();
	}

	@Override
	public String toString() {
		return "SingletonTargetSource for target object [" + ObjectUtils.identityToString(this.target) + "]";
	}

}