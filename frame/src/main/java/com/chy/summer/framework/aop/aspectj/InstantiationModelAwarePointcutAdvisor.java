package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.aop.PointcutAdvisor;

/**
 * 由Advisors实现的接口，其中包装了可能具有惰性初始化策略的AspectJ切面，这将意味着advice的延迟初始化。
 */
public interface InstantiationModelAwarePointcutAdvisor extends PointcutAdvisor {

	/**
	 * 判断这个advisor是否需要延迟初始化advice
	 */
	boolean isLazy();

	/**
	 * 返回此advisor是否已实例化其advice。
	 */
	boolean isAdviceInstantiated();

}