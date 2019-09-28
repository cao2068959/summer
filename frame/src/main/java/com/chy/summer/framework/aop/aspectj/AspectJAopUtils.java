package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.AfterAdvice;
import com.chy.summer.framework.aop.BeforeAdvice;
import com.chy.summer.framework.aop.aopalliance.Advice;
import com.sun.istack.internal.Nullable;

/**
 * 使用advisors的工具类
 */
public abstract class AspectJAopUtils {

	/**
	 * 如果advisors是一种事前通知，则返回true
	 */
	public static boolean isBeforeAdvice(Advisor anAdvisor) {
		AspectJPrecedenceInformation precedenceInfo = getAspectJPrecedenceInformationFor(anAdvisor);
		if (precedenceInfo != null) {
			return precedenceInfo.isBeforeAdvice();
		}
		return (anAdvisor.getAdvice() instanceof BeforeAdvice);
	}

	/**
	 * 如果advisors是一种事后通知，则返回true
	 */
	public static boolean isAfterAdvice(Advisor anAdvisor) {
		AspectJPrecedenceInformation precedenceInfo = getAspectJPrecedenceInformationFor(anAdvisor);
		if (precedenceInfo != null) {
			return precedenceInfo.isAfterAdvice();
		}
		return (anAdvisor.getAdvice() instanceof AfterAdvice);
	}

	/**
	 * 获取advice/advisors的优先顺序信息
	 * 如果没有设置优先级则返回null
	 */
	@Nullable
	public static AspectJPrecedenceInformation getAspectJPrecedenceInformationFor(Advisor anAdvisor) {
		//判断类型
		if (anAdvisor instanceof AspectJPrecedenceInformation) {
			return (AspectJPrecedenceInformation) anAdvisor;
		}
		//获取通知
		Advice advice = anAdvisor.getAdvice();
		if (advice instanceof AspectJPrecedenceInformation) {
			return (AspectJPrecedenceInformation) advice;
		}
		return null;
	}
}