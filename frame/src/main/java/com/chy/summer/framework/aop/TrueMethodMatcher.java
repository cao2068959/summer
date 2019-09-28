package com.chy.summer.framework.aop;

import com.sun.istack.internal.Nullable;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 方法匹配的规范
 */
class TrueMethodMatcher implements MethodMatcher, Serializable {

	public static final TrueMethodMatcher INSTANCE = new TrueMethodMatcher();

	/**
	 * 单例模式
	 */
	private TrueMethodMatcher() {
	}


	@Override
	public boolean isRuntime() {
		return false;
	}

	@Override
	public boolean matches(Method method, @Nullable Class<?> targetClass) {
		return true;
	}

	@Override
	public boolean matches(Method method, @Nullable Class<?> targetClass, Object... args) {
		throw new UnsupportedOperationException();
	}


	@Override
	public String toString() {
		return "MethodMatcher.TRUE";
	}

	/**
	 * 序列化支持
	 */
	private Object readResolve() {
		return INSTANCE;
	}

}