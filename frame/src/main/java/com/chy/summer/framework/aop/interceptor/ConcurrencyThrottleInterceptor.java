package com.chy.summer.framework.aop.interceptor;

import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.chy.summer.framework.util.core.ConcurrencyThrottleSupport;

import java.io.Serializable;

/**
 * 并发限制拦截器
 * 拦截器可限制并发访问，如果达到指定的并发限制，则会阻止调用
 * 应用于涉及大量系统资源的本地服务的方法，
 * 在这种情况下，为特定的服务限制并发性比限制整个线程池(例如，web容器的线程池)更有效。
 * 此拦截器的默认并发限制为1。指定“concurrencyLimit”bean属性来更改这个值
 */
public class ConcurrencyThrottleInterceptor extends ConcurrencyThrottleSupport
		implements MethodInterceptor, Serializable {

	public ConcurrencyThrottleInterceptor() {
		setConcurrencyLimit(1);
	}

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		beforeAccess();
		try {
			return methodInvocation.proceed();
		}
		finally {
			afterAccess();
		}
	}

}