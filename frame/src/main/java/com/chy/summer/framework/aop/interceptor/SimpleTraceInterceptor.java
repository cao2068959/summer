package com.chy.summer.framework.aop.interceptor;

import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;

/**
 * 简单的AOP Alliance MethodInterceptor，
 * 可以被引入到一个链中来显示关于被拦截的方法调用的详细跟踪信息，带有方法调用前和方法调用后信息
 */
public class SimpleTraceInterceptor extends AbstractTraceInterceptor {

	/**
	 * 创建一个新的静态SimpleTraceInterceptor
	 */
	public SimpleTraceInterceptor() {
	}

	/**
	 * 根据给定的标记，使用动态或静态日志记录器创建一个新的SimpleTraceInterceptor
	 * @param useDynamicLogger 是使用动态记录器还是静态记录器
	 */
	public SimpleTraceInterceptor(boolean useDynamicLogger) {
		setUseDynamicLogger(useDynamicLogger);
	}

	/**
	 * 围绕所提供的方法调用的任何跟踪。子类负责通过调用methodinvoc.proceed()来确保方法调用实际执行。
	 * @param invocation 需要
	 * @param logger 要写入跟踪消息的Logger
	 */
	@Override
	protected Object invokeUnderTrace(MethodInvocation invocation, Logger logger) throws Throwable {
		String invocationDescription = getInvocationDescription(invocation);
		writeToLog(logger, "进入 " + invocationDescription);
		try {
			Object rval = invocation.proceed();
			writeToLog(logger, "进入 " + invocationDescription);
			return rval;
		}
		catch (Throwable ex) {
			writeToLog(logger, "抛出异常 " + invocationDescription, ex);
			throw ex;
		}
	}

	/**
	 * 返回给定方法调用的描述
	 * @param invocation 调用的方法
	 */
	protected String getInvocationDescription(MethodInvocation invocation) {
		return "["+invocation.getThis().getClass().getName()+"]类的方法"+ invocation.getMethod().getName();
	}

}