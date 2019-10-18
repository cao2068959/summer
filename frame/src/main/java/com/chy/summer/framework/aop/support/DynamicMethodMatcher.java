package com.chy.summer.framework.aop.support;

import com.chy.summer.framework.aop.MethodMatcher;
import com.sun.istack.internal.Nullable;

import java.lang.reflect.Method;

/**
 * 方便的动态方法匹配器的抽象超类，在运行时关心参数
 */
public abstract class DynamicMethodMatcher implements MethodMatcher {

	/**
	 * 这个方法匹配器是否动态的，等于true的时候调用2个参数的matches()
	 */
	@Override
	public final boolean isRuntime() {
		return true;
	}

	/**
	 * 执行静态匹配，看给定的方法是否匹配
	 * 可以覆盖以添加动态匹配的前提条件。 此实现始终返回true。
	 */
	@Override
	public boolean matches(Method method, @Nullable Class<?> targetClass) {
		return true;
	}

}