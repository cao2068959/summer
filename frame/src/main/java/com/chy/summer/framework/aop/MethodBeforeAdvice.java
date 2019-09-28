package com.chy.summer.framework.aop;

import com.sun.istack.internal.Nullable;

import java.lang.reflect.Method;

/**
 * 调用方法之前调用的通知。
 * 这样的通知不能阻止方法继续进行调用，除非抛出一个Throwable。
 */
public interface MethodBeforeAdvice extends BeforeAdvice {

	/**
	 * 调用给定方法之前的回调
	 * @param method 调用的方法
	 * @param args 方法参数
	 * @param target 方法调用的目标
	 */
	void before(Method method, Object[] args, @Nullable Object target) throws Throwable;

}