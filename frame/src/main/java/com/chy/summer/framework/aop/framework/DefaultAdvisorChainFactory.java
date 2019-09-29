package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.IntroductionAdvisor;
import com.chy.summer.framework.aop.MethodMatcher;
import com.chy.summer.framework.aop.PointcutAdvisor;
import com.chy.summer.framework.aop.aopalliance.intercept.Interceptor;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;
import com.chy.summer.framework.aop.framework.adapter.AdvisorAdapterRegistry;
import com.chy.summer.framework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import com.chy.summer.framework.aop.support.MethodMatchers;
import com.sun.istack.internal.Nullable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 给定advised对象的情况下，为方法制定一种简单但确定的advice链
 */
public class DefaultAdvisorChainFactory implements AdvisorChainFactory, Serializable {

	/**
	 * 从提供的配置实例config中获取advisor列表,遍历处理这些advisor.如果是IntroductionAdvisor,
	 * 则判断此Advisor能否应用到目标类targetClass上.如果是PointcutAdvisor,则判断
	 * 此Advisor能否应用到目标方法method上.将满足条件的Advisor通过AdvisorAdaptor转化成Interceptor列表返回.
	 */
	@Override
	public List<Object> getInterceptorsAndDynamicInterceptionAdvice(
            Advised config, Method method, @Nullable Class<?> targetClass) {

		List<Object> interceptorList = new ArrayList<>(config.getAdvisors().length);
		Class<?> actualClass = (targetClass != null ? targetClass : method.getDeclaringClass());
		//查看是否包含IntroductionAdvisor
		boolean hasIntroductions = hasMatchingIntroductions(config, actualClass);
		//这里实际上注册一系列AdvisorAdapter,用于将Advisor转化成MethodInterceptor
		AdvisorAdapterRegistry registry = GlobalAdvisorAdapterRegistry.getInstance();

		for (Advisor advisor : config.getAdvisors()) {
			if (advisor instanceof PointcutAdvisor) {
				//处理切入点advisor
				PointcutAdvisor pointcutAdvisor = (PointcutAdvisor) advisor;
				//判断方法所以在的类是否与advisor匹配
				if (config.isPreFiltered() || pointcutAdvisor.getPointcut().getClassFilter().matches(actualClass)) {
					//将Advisor转化成Interceptor
					MethodInterceptor[] interceptors = registry.getInterceptors(advisor);
					//检查当前advisor的pointcut是否可以匹配当前方法
					MethodMatcher mm = pointcutAdvisor.getPointcut().getMethodMatcher();
					if (MethodMatchers.matches(mm, method, actualClass, hasIntroductions)) {
						//判断是否动态
						if (mm.isRuntime()) {
							//缓存拦截器链
							for (MethodInterceptor interceptor : interceptors) {
								//将拦截器和匹配器封装
								interceptorList.add(new InterceptorAndDynamicMethodMatcher(interceptor, mm));
							}
						}
						else {
							//缓存拦截器链
							interceptorList.addAll(Arrays.asList(interceptors));
						}
					}
				}
			}
			else if (advisor instanceof IntroductionAdvisor) {
				IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
				//进行类型匹配
				if (config.isPreFiltered() || ia.getClassFilter().matches(actualClass)) {
					//获取对应的拦截器
					Interceptor[] interceptors = registry.getInterceptors(advisor);
					//缓存拦截器链
					interceptorList.addAll(Arrays.asList(interceptors));
				}
			}
			else {
				Interceptor[] interceptors = registry.getInterceptors(advisor);
				interceptorList.addAll(Arrays.asList(interceptors));
			}
		}

		return interceptorList;
	}

	/**
     * 判断advisor是否包含匹配的introductions
	 */
	private static boolean hasMatchingIntroductions(Advised config, Class<?> actualClass) {
	    //循环检查所有的advisor
		for (int i = 0; i < config.getAdvisors().length; i++) {
			Advisor advisor = config.getAdvisors()[i];
			//只有实现了IntroductionAdvisor才可能包含指定的introductions
			if (advisor instanceof IntroductionAdvisor) {
				IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
				//获取类过滤器，与指定类匹配
				if (ia.getClassFilter().matches(actualClass)) {
					return true;
				}
			}
		}
		return false;
	}

}