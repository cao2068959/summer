package com.chy.summer.framework.aop.framework.adapter;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.aopalliance.Advice;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;

/**
 * 实现该接口可以处理新的Advisor和Advice类型，扩展AOP框架
 * 实现对象可以根据自定义advice类型创建AOP拦截器，从而使这些advice类型可以在AOP框架中使用
 *
 * 大多数用户无需实现此接口。仅在需要在summer中引入更多Advisor或Advice类型时才这样做。
 */
public interface AdvisorAdapter {

	/**
	 * 此适配器是否解析这个advice对象？
	 * 用带有这个advice作为参数的Advisor调用getInterceptors方法是否有效？
	 */
	boolean supportsAdvice(Advice advice);

	/**
	 * 返回一个AOP Alliance MethodInterceptor，将给定advice的行为暴露给AOP框架拦截。
	 */
	MethodInterceptor getInterceptor(Advisor advisor);

}