package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.aop.AfterAdvice;
import com.chy.summer.framework.aop.AfterReturningAdvice;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.TypeUtils;
import javax.annotation.Nullable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 方法返回通知
 */
public class AspectJAfterReturningAdvice extends AbstractAspectJAdvice
		implements AfterReturningAdvice, AfterAdvice, Serializable {

	/**
	 * 初始化方法执行后通知
	 */
	public AspectJAfterReturningAdvice(
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
	 * 设置返回名称
	 */
	@Override
	public void setReturningName(String name) {
		setReturningNameNoCheck(name);
	}

	/**
	 * 成功返回给定方法后的回调
	 * @param returnValue 方法的返回值
	 * @param method 调用的方法
	 * @param args 方法参数
	 * @param target 方法调用的目标
	 * @throws Throwable
	 */
	@Override
	public void afterReturning(@Nullable Object returnValue, Method method, Object[] args, @Nullable Object target) throws Throwable {
		if (shouldInvokeOnReturnValueOf(method, returnValue)) {
			invokeAdviceMethod(getJoinPointMatch(), returnValue, null);
		}
	}


	/**
	 * 如果指定了返回类型，则仅在返回值是给定返回类型的实例（如果有泛型类型参数，则也需相等）与分配规则匹配的情况下才调用advice
	 * 如果返回类型为Object，则始终会调用advice
	 */
	private boolean shouldInvokeOnReturnValueOf(Method method, @Nullable Object returnValue) {
		Class<?> type = getDiscoveredReturningType();
		Type genericType = getDiscoveredReturningGenericType();
		//返回值相匹配 并且
		// 通用返回类型与返回类型相等或者为空
		// 或者按照Java泛型规则是否可以将右侧类型分配给左侧类型
		return (matchesReturnValue(type, method, returnValue) &&
				(genericType == null || genericType == type ||
						TypeUtils.isAssignable(genericType, method.getGenericReturnType())));
	}

	/**
	 * 如果返回值为null（或返回类型为void），
	 * 则使用目标方法的返回类型来确定是否调用通知。
	 * 同样，即使返回类型为void，如果advice方法中声明的参数类型为Object，则该通知仍必须被调用。
	 * @param type 通知方法声明的参数类型
	 * @param method 通知方法
	 * @param returnValue 目标方法的返回值
	 * @return whether to invoke the advice method for the given return value and type
	 */
	private boolean matchesReturnValue(Class<?> type, Method method, @Nullable Object returnValue) {
		if (returnValue != null) {
			//返回值不为空
			return ClassUtils.isAssignableValue(type, returnValue);
		} else if (Object.class == type && void.class == method.getReturnType()) {
			//通知方法声明的参数类型为Object 并且通知方法没有参数
			return true;
		} else {
			return ClassUtils.isAssignable(type, method.getReturnType());
		}
	}

}