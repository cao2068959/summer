package com.chy.summer.framework.aop;

import com.sun.istack.internal.Nullable;

import java.lang.reflect.Method;

/**
 * 特殊类型的MethodMatcher方法在匹配方法
 * 在没有目标class的introductions的情况下，方法匹配器可以更有效的匹配
 */
public interface IntroductionAwareMethodMatcher extends MethodMatcher {

	/**
	 * 执行静态检查给定的方法是否匹配
	 */
	boolean matches(Method method, @Nullable Class<?> targetClass, boolean hasIntroductions);

}