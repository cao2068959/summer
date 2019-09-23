package com.chy.summer.framework.aop;

/**
 * 类过滤器
 * 用于约束一个Advisor，与指定的目标对象是否匹配。
 * 只有匹配的前提下，Advisor才能使用其内部持有的Advice对目标对象进行增强
 */
@FunctionalInterface
public interface ClassFilter {

	/**
	 * 切入点应该应用于给定的接口还是类
	 * @param clazz clazz候选目标类型
	 * @return 返回通知是否应用于给定的类
	 */
	boolean matches(Class<?> clazz);


	/**
	 * 匹配所有类的类过滤器的规范
	 */
	ClassFilter TRUE = TrueClassFilter.INSTANCE;
}