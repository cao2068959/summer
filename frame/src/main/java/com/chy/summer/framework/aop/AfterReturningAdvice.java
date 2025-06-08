package com.chy.summer.framework.aop;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * 返回通知仅在正常方法返回时调用，异常时不会触发。
 * 这个通知可以看到返回值，但不能更改它。
 */
public interface AfterReturningAdvice extends AfterAdvice {

	/**
	 * 成功返回给定方法后的回调
	 * @param returnValue 方法的返回值
	 * @param method 调用的方法
	 * @param args 方法参数
	 * @param target 方法调用的目标
	 */
	void afterReturning(@Nullable Object returnValue, Method method, Object[] args, @Nullable Object target) throws Throwable;
}