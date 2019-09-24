package com.chy.summer.framework.aop.support;

import com.chy.summer.framework.aop.ClassFilter;
import com.chy.summer.framework.aop.MethodMatcher;
import com.chy.summer.framework.aop.Pointcut;
import com.chy.summer.framework.util.Assert;

import java.io.Serializable;

/**
 * 用于构建切入点的类。使得构建切入点更加便捷
 * 所有方法都返回ComposablePointcut，所以我们可以使用这样一个简洁的习惯用法:
 * Pointcut pc = new ComposablePointcut().union(classFilter).intersection(methodMatcher).intersection(pointcut);
 */
public class ComposablePointcut implements Pointcut, Serializable {

	/**
	 * 类过滤器
	 */
	private ClassFilter classFilter;
	/**
	 * 方法匹配器
	 */
	private MethodMatcher methodMatcher;


	/**
	 * 使用ClassFilter和MethodMatcher创建一个默认的ComposablePointcut
	 */
	public ComposablePointcut() {
		this.classFilter = ClassFilter.TRUE;
		this.methodMatcher = MethodMatcher.TRUE;
	}

	/**
	 * 根据给定的切入点创建一个组合切入点
	 */
	public ComposablePointcut(Pointcut pointcut) {
		Assert.notNull(pointcut, "切点不可为空");
		this.classFilter = pointcut.getClassFilter();
		this.methodMatcher = pointcut.getMethodMatcher();
	}

	/**
	 * 根据给定的ClassFilter创建一个组合切入点
	 */
	public ComposablePointcut(ClassFilter classFilter) {
		Assert.notNull(classFilter, "ClassFilter不可为空");
		this.classFilter = classFilter;
		this.methodMatcher = MethodMatcher.TRUE;
	}

	/**
	 * 根据给定的MethodMatcher创建一个组合切入点
	 */
	public ComposablePointcut(MethodMatcher methodMatcher) {
		Assert.notNull(methodMatcher, "MethodMatcher不可为空");
		this.classFilter = ClassFilter.TRUE;
		this.methodMatcher = methodMatcher;
	}

	/**
	 * 根据给定的classFilter和MethodMatcher创建一个组合切入点
	 */
	public ComposablePointcut(ClassFilter classFilter, MethodMatcher methodMatcher) {
		Assert.notNull(classFilter, "ClassFilter不可为空");
		Assert.notNull(methodMatcher, "MethodMatcher不可为空");
		this.classFilter = classFilter;
		this.methodMatcher = methodMatcher;
	}


	/**
	 * 使用给定的类过滤器与当前类过滤器做并集
	 */
	public ComposablePointcut union(ClassFilter other) {
		this.classFilter = ClassFilters.union(this.classFilter, other);
		return this;
	}

	/**
	 * 使用给定的类过滤器与当前类过滤器做交集
	 */
	public ComposablePointcut intersection(ClassFilter other) {
		this.classFilter = ClassFilters.intersection(this.classFilter, other);
		return this;
	}

	/**
	 * 使用给定的方法匹配器与方法匹配器做并集
	 */
	public ComposablePointcut union(MethodMatcher other) {
		this.methodMatcher = MethodMatchers.union(this.methodMatcher, other);
		return this;
	}

	/**
	 * 使用给定的方法匹配器与方法匹配器做交集
	 */
	public ComposablePointcut intersection(MethodMatcher other) {
		this.methodMatcher = MethodMatchers.intersection(this.methodMatcher, other);
		return this;
	}

	/**
	 * 使用给定的切点做并集
	 * 方法匹配器只有在它们的类过滤器来自于同一个原始切入点的时候才匹配
	 */
	public ComposablePointcut union(Pointcut other) {
		this.methodMatcher = MethodMatchers.union(
				this.methodMatcher, this.classFilter, other.getMethodMatcher(), other.getClassFilter());
		this.classFilter = ClassFilters.union(this.classFilter, other.getClassFilter());
		return this;
	}

	/**
	 * 使用给定切入点做交集
	 */
	public ComposablePointcut intersection(Pointcut other) {
		this.classFilter = ClassFilters.intersection(this.classFilter, other.getClassFilter());
		this.methodMatcher = MethodMatchers.intersection(this.methodMatcher, other.getMethodMatcher());
		return this;
	}

	/**
	 * 获取类过滤器
	 */
	@Override
	public ClassFilter getClassFilter() {
		return this.classFilter;
	}

	/**
	 * 获取方法匹配器
	 */
	@Override
	public MethodMatcher getMethodMatcher() {
		return this.methodMatcher;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ComposablePointcut)) {
			return false;
		}
		ComposablePointcut otherPointcut = (ComposablePointcut) other;
		return (this.classFilter.equals(otherPointcut.classFilter) &&
				this.methodMatcher.equals(otherPointcut.methodMatcher));
	}

	@Override
	public int hashCode() {
		return this.classFilter.hashCode() * 37 + this.methodMatcher.hashCode();
	}

	@Override
	public String toString() {
		return "ComposablePointcut: " + this.classFilter + ", " +this.methodMatcher;
	}

}