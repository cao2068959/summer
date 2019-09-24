package com.chy.summer.framework.aop;

import com.sun.istack.internal.Nullable;

/**
 * TargetSource用于获取AOP调用的当前目标对象，如果没有around通知选择拦截器链本身，则将通过反射调用该目标。
 * 如果TargetSource是静态，它将始终返回相同的目标。
 * 动态目标源可以支持池、热交换等功能。
 * 码农们通常不需要直接使用TargetSources。
 */
public interface TargetSource extends TargetClassAware {

	/**
	 * 返回目标实例的类型
	 * 可以返回null，某些用法可能只适用于预先确定的目标类
	 */
	@Override
	@Nullable
	Class<?> getTargetClass();

	/**
	 * 是否每次调用getTarget()都返回相同的对象
	 * 在这种情况下，将不会调用releaseTarget(Object)，并且AOP框架可以对返回值进行缓存。
	 */
	boolean isStatic();

	/**
	 * 返回目标实例
	 */
	@Nullable
	Object getTarget() throws Exception;

	/**
	 * 释放从getTarget()方法中获得的给定目标对象
	 * @param target 通过调用getTarget()获得的对象
	 */
	void releaseTarget(Object target) throws Exception;

}