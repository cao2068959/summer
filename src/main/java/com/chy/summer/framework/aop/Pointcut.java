package com.chy.summer.framework.aop;

/**
 * 切点的抽象概念
 *
 * 一个切点包含类过滤器和方法匹配器
 */
public interface Pointcut {

	/**
	 * 获取类过滤器
	 */
	ClassFilter getClassFilter();

	/**
	 * 获取方法匹配器
	 */
	MethodMatcher getMethodMatcher();

	/**
	 * 匹配所有切点的规范类实例
	 */
	Pointcut TRUE = TruePointcut.INSTANCE;

}