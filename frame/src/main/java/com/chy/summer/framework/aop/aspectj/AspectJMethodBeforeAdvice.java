package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.aop.MethodBeforeAdvice;
import javax.annotation.Nullable;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 前置通知
 */
public class AspectJMethodBeforeAdvice extends AbstractAspectJAdvice implements MethodBeforeAdvice, Serializable {

	/**
	 * 初始化
	 */
	public AspectJMethodBeforeAdvice(
			Method aspectJBeforeAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aif) {

		super(aspectJBeforeAdviceMethod, pointcut, aif);
	}

	/**
	 * 执行方法
	 * @param method 调用的方法
	 * @param args 方法参数
	 * @param target 方法调用的目标
	 * @throws Throwable
	 */
	@Override
	public void before(Method method, Object[] args, @Nullable Object target) throws Throwable {
		invokeAdviceMethod(getJoinPointMatch(), null, null);
	}

	/**
	 * 判断是否为前置通知
	 */
	@Override
	public boolean isBeforeAdvice() {
		return true;
	}

	/**
	 * 判断是否为后置通知
	 */
	@Override
	public boolean isAfterAdvice() {
		return false;
	}

}