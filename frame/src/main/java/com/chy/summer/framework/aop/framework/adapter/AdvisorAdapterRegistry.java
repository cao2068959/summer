package com.chy.summer.framework.aop.framework.adapter;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;

/**
 * Advisor适配器注册表的接口
 */
public interface AdvisorAdapterRegistry {

	/**
	 * 包装给定advice的Advisor，不会返回null
	 */
	Advisor wrap(Object advice) throws UnknownAdviceTypeException;

	/**
	 * 返回一个AOP Alliance MethodInterceptor数组，使其在基于拦截的框架中使用给定的Advisor。
	 * 如果它是PointcutAdvisor，则只需返回拦截器即可。
	 */
	MethodInterceptor[] getInterceptors(Advisor advisor) throws UnknownAdviceTypeException;

	/**
	 * 注册给定的AdvisorAdapter
	 */
	void registerAdvisorAdapter(AdvisorAdapter adapter);

}