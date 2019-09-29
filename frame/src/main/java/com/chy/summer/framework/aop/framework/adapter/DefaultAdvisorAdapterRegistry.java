package com.chy.summer.framework.aop.framework.adapter;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.aopalliance.Advice;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;
import com.chy.summer.framework.aop.support.DefaultPointcutAdvisor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * AdvisorAdapterRegistry接口的默认实现
 * 用来支撑支持MethodInterceptor， MethodBeforeAdvice， AfterReturningAdvice， ThrowsAdvice
 */
public class DefaultAdvisorAdapterRegistry implements AdvisorAdapterRegistry, Serializable {

	private final List<AdvisorAdapter> adapters = new ArrayList<>(3);


	/**
	 * 创建一个新的DefaultAdvisorAdapterRegistry
	 */
	public DefaultAdvisorAdapterRegistry() {
		registerAdvisorAdapter(new MethodBeforeAdviceAdapter());
		registerAdvisorAdapter(new AfterReturningAdviceAdapter());
		registerAdvisorAdapter(new ThrowsAdviceAdapter());
	}

	/**
	 * 包装给定的advice返回Advisor
	 * 默认情况下应至少支持 MethodInterceptor， MethodBeforeAdvice， AfterReturningAdvice， ThrowsAdvice。
	 */
	@Override
	public Advisor wrap(Object adviceObject) throws UnknownAdviceTypeException {
		if (adviceObject instanceof Advisor) {
			return (Advisor) adviceObject;
		}
		if (!(adviceObject instanceof Advice)) {
			throw new UnknownAdviceTypeException(adviceObject);
		}
		Advice advice = (Advice) adviceObject;
		if (advice instanceof MethodInterceptor) {
			return new DefaultPointcutAdvisor(advice);
		}
		for (AdvisorAdapter adapter : this.adapters) {
			// 检查是否受支持
			if (adapter.supportsAdvice(advice)) {
				return new DefaultPointcutAdvisor(advice);
			}
		}
		throw new UnknownAdviceTypeException(advice);
	}

	/**
	 * 返回一个AOP Alliance MethodInterceptor数组，使其在基于拦截的框架中使用给定的Advisor
	 * 如果它是PointcutAdvisor，则只需返回拦截器即可
	 */
	@Override
	public MethodInterceptor[] getInterceptors(Advisor advisor) throws UnknownAdviceTypeException {
		List<MethodInterceptor> interceptors = new ArrayList<>(3);
		//获取advice
		Advice advice = advisor.getAdvice();
		if (advice instanceof MethodInterceptor) {
			//如果这个advice是一个MethodInterceptor，将其添加到拦截器列表中
			interceptors.add((MethodInterceptor) advice);
		}
		//使用所有适配器逐一判断能否解析这个advice
		for (AdvisorAdapter adapter : this.adapters) {
			if (adapter.supportsAdvice(advice)) {
				//获取对应的拦截器
				interceptors.add(adapter.getInterceptor(advisor));
			}
		}
		if (interceptors.isEmpty()) {
			throw new UnknownAdviceTypeException(advisor.getAdvice());
		}
		//返回这个方法所有的拦截器
		return interceptors.toArray(new MethodInterceptor[interceptors.size()]);
	}

	/**
	 * 注册给定的AdvisorAdapter
	 */
	@Override
	public void registerAdvisorAdapter(AdvisorAdapter adapter) {
		this.adapters.add(adapter);
	}

}