package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.aop.AfterAdvice;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 异常通知
 */
public class AspectJAfterThrowingAdvice extends AbstractAspectJAdvice
		implements MethodInterceptor, AfterAdvice, Serializable {

	/**
	 * 初始化异常通知
	 */
	public AspectJAfterThrowingAdvice(
			Method aspectJBeforeAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aif) {
		super(aspectJBeforeAdviceMethod, pointcut, aif);
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

	/**
	 * 设置抛出名称
	 */
	@Override
	public void setThrowingName(String name) {
		setThrowingNameNoCheck(name);
	}

	/**
	 * 执行方法
	 */
	@Override
	public Object invoke(MethodInvocation mi) throws Throwable {
		try {
			return mi.proceed();
		}
		catch (Throwable ex) {
			if (shouldInvokeOnThrowing(ex)) {
				invokeAdviceMethod(getJoinPointMatch(), null, ex);
			}
			throw ex;
		}
	}

	/**
	 * 当抛出的异常是给定抛出类型的子类型之后，才调用指定抛出的Advice
	 * 实际就是判断异常的类型与指定的异常是否一致，如果相同或者是指定异常的子类型，就执行通知
	 */
	private boolean shouldInvokeOnThrowing(Throwable ex) {
		return getDiscoveredThrowingType().isAssignableFrom(ex.getClass());
	}

}