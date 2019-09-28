package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.PointcutAdvisor;
import com.chy.summer.framework.aop.interceptor.ExposeInvocationInterceptor;

import java.util.List;

/**
 * AspectJ代理的工具类
 */
public abstract class AspectJProxyUtils {

	/**
	 * 必要时添加特殊顾问，与包含AspectJ顾问的代理链一起使用。
	 * 这将暴露当前的AOP的调用（某些AspectJ切入点匹配所必需），
	 * 并使当前的AspectJ JoinPoint可用。 如果顾问链中没有AspectJ顾问，则该调用将无效。
	 */
	public static boolean makeAdvisorChainAspectJCapableIfNecessary(List<Advisor> advisors) {
		if (!advisors.isEmpty()) {
			boolean foundAspectJAdvice = false;
			for (Advisor advisor : advisors) {
				//判断advisor是否包含AspectJAdvice
				if (isAspectJAdvice(advisor)) {
					foundAspectJAdvice = true;
				}
			}
			if (foundAspectJAdvice && !advisors.contains(ExposeInvocationInterceptor.ADVISOR)) {
				//添加暴露方法执行器的拦截器
				advisors.add(0, ExposeInvocationInterceptor.ADVISOR);
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断给定的Advisor是否包含AspectJAdvice。
	 */
	private static boolean isAspectJAdvice(Advisor advisor) {
		//主要是判断基础的父类和实现的接口来判断
		return (advisor instanceof InstantiationModelAwarePointcutAdvisor ||
				advisor.getAdvice() instanceof AbstractAspectJAdvice ||
				(advisor instanceof PointcutAdvisor &&
						 ((PointcutAdvisor) advisor).getPointcut() instanceof AspectJExpressionPointcut));
	}

}