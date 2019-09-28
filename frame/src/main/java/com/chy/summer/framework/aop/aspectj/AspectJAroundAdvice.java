package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.aop.ProxyMethodInvocation;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.weaver.tools.JoinPointMatch;

import java.io.Serializable;
import java.lang.reflect.Method;

public class AspectJAroundAdvice extends AbstractAspectJAdvice implements MethodInterceptor, Serializable {

	public AspectJAroundAdvice(
			Method aspectJAroundAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aif) {

		super(aspectJAroundAdviceMethod, pointcut, aif);
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
		return false;
	}

	@Override
	protected boolean supportsProceedingJoinPoint() {
		return true;
	}

	/**
	 * 执行方法
	 */
	@Override
	public Object invoke(MethodInvocation mi) throws Throwable {
		if (!(mi instanceof ProxyMethodInvocation)) {
			throw new IllegalStateException("方法执行器不是Summer的代理方法执行器: " + mi);
		}
		//转成代理方法执行器
		ProxyMethodInvocation pmi = (ProxyMethodInvocation) mi;
		//封装成延迟初始化的执行器
		ProceedingJoinPoint pjp = lazyGetProceedingJoinPoint(pmi);
		//方法匹配器
		JoinPointMatch jpm = getJoinPointMatch(pmi);
		//执行通知方法
		return invokeAdviceMethod(pjp, jpm, null, null);
	}

	/**
	 * 返回当前调用的ProceedingJoinPoint，如果尚未将其绑定到线程，则延迟实例化。
	 */
	protected ProceedingJoinPoint lazyGetProceedingJoinPoint(ProxyMethodInvocation rmi) {
		return new MethodInvocationProceedingJoinPoint(rmi);
	}

}