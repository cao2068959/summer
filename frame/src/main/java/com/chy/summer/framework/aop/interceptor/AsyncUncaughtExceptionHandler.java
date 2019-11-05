package com.chy.summer.framework.aop.interceptor;

import java.lang.reflect.Method;

/**
 * 处理从异步方法抛出的未捕获异常的策略
 */
@FunctionalInterface
public interface AsyncUncaughtExceptionHandler {

	/**
	 * 处理从异步方法抛出的给定未捕获异常
	 * @param ex 从异步方法引发的异常
	 * @param method 异步方法
	 * @param params 用于调用方法的参数
	 */
	void handleUncaughtException(Throwable ex, Method method, Object... params);

}