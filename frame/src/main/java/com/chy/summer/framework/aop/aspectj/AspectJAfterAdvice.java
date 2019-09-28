package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.aop.AfterAdvice;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 后置通知
 */
public class AspectJAfterAdvice extends AbstractAspectJAdvice
		implements MethodInterceptor, AfterAdvice, Serializable {

	/**
	 * 初始化后置通知
	 * @param aspectJBeforeAdviceMethod AspectJ风格的通知方法
	 * @param pointcut AspectJ表达式切入点
	 * @param aif 切面实例的工厂
	 */
	public AspectJAfterAdvice(
			Method aspectJBeforeAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aif) {
		super(aspectJBeforeAdviceMethod, pointcut, aif);
	}

	/**
	 * 执行后置方法
	 */
	@Override
	public Object invoke(MethodInvocation mi) throws Throwable {
		try {
			return mi.proceed();
		}
		finally {
			invokeAdviceMethod(getJoinPointMatch(), null, null);
		}
	}

	/**
	 * 判断是否为前置通知
	 */
	@Override
	public boolean isBeforeAdvice() {
		return false;
	}

	/**
	 * 判断是否为后置通知
	 */
	@Override
	public boolean isAfterAdvice() {
		return true;
	}

}