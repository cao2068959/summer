package com.chy.summer.framework.aop.support;

import com.chy.summer.framework.aop.ClassFilter;
import com.chy.summer.framework.aop.MethodMatcher;
import com.chy.summer.framework.aop.Pointcut;

/**
 * 当我们想要强制子类实现MethodMatcher接口，但是子类想要成为切入点时,继承这个类
 * 可以覆盖getClassFilter()方法来定制ClassFilter行为
 */
public abstract class DynamicMethodMatcherPointcut extends DynamicMethodMatcher implements Pointcut {

	/**
	 * 获取类过滤器
	 */
	@Override
	public ClassFilter getClassFilter() {
		return ClassFilter.TRUE;
	}

	/**
	 * 获取方法匹配器
	 */
	@Override
	public final MethodMatcher getMethodMatcher() {
		return this;
	}

}