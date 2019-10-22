package com.chy.summer.framework.aop.support;

import com.chy.summer.framework.aop.ClassFilter;
import com.chy.summer.framework.aop.MethodMatcher;
import com.chy.summer.framework.aop.Pointcut;

/**
 * 当我们要强制子类实现MethodMatcher接口但子类将成为切入点时，提供方便的父类
 */
public abstract class StaticMethodMatcherPointcut extends StaticMethodMatcher implements Pointcut {

	private ClassFilter classFilter = ClassFilter.TRUE;


	/**
	 * 设置类过滤器，默认过滤放行所有类
	 */
	public void setClassFilter(ClassFilter classFilter) {
		this.classFilter = classFilter;
	}

	@Override
	public ClassFilter getClassFilter() {
		return this.classFilter;
	}


	@Override
	public final MethodMatcher getMethodMatcher() {
		return this;
	}
}