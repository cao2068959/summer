package com.chy.summer.framework.aop.interceptor;

import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.chy.summer.framework.aop.support.AopUtils;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * 跟踪的MethodInterceptor基本实现。
 * 默认情况下，日志消息将被写入拦截器类的日志，而不是被拦截的类
 * 将useDynamicLogger bean属性设置为true会导致所有日志消息都被写入到针对目标类的日志中
 */
public abstract class AbstractTraceInterceptor implements MethodInterceptor, Serializable {

	/**
	 * 用于编写跟踪消息的默认日志实例
	 */
	@Nullable
	private Logger defaultLogger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 使用动态记录器时是否应隐藏代理类名称
	 */
	private boolean hideProxyClassNames = false;

	/**
	 * 是否将异常传递给记录器
	 */
	private boolean logExceptionStackTrace = true;


	/**
	 * 设置使用动态记录器还是静态记录器
	 * 默认使用静态记录器的跟踪拦截器。
	 */
	public void setUseDynamicLogger(boolean useDynamicLogger) {
		//如果未使用默认记录器，则将其释放
		this.defaultLogger = (useDynamicLogger ? null : LoggerFactory.getLogger(this.getClass()));
	}

	/**
	 * 设置要使用的记录器的名称
	 * 该名称将通过Commons Logging传递给基础记录器实现，根据记录器的配置将其解释为日志类别。
	 */
	public void setLoggerName(String loggerName) {
		this.defaultLogger = LoggerFactory.getLogger(loggerName);
	}

	/**
	 * 设置使用动态记录器时是否应隐藏代理类名称
	 * 默认值为false
	 */
	public void setHideProxyClassNames(boolean hideProxyClassNames) {
		this.hideProxyClassNames = hideProxyClassNames;
	}

	/**
	 * 设置是否将异常传递给记录器，建议将其堆栈跟踪包括在日志中
	 * 默认值为“ true”； 将其设置为“ false”，以便将日志输出减少为仅跟踪消息（如果适用，可能包括异常类名称和异常消息）。
	 */
	public void setLogExceptionStackTrace(boolean logExceptionStackTrace) {
		this.logExceptionStackTrace = logExceptionStackTrace;
	}


	/**
	 * 确定是否为特定的MethodInvocation启用日志记录
	 * 如果不是，则方法调用照常进行，否则将方法调用传递给invokeUnderTrace方法进行处理
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Logger logger = getLoggerForInvocation(invocation);
		if (isInterceptorEnabled(invocation, logger)) {
			return invokeUnderTrace(invocation, logger);
		}
		else {
			return invocation.proceed();
		}
	}

	/**
	 * 返回用于给定方法调用的适当日志实例。
	 * 如果设置了useDynamicLogger标志，日志实例将是MethodInvocation的目标类的日志实例，否则日志将是默认的静态日志记录器
	 * @param invocation 追踪的方法调用器
	 */
	protected Logger getLoggerForInvocation(MethodInvocation invocation) {
		if (this.defaultLogger != null) {
			return this.defaultLogger;
		}
		else {
			Object target = invocation.getThis();
			return LoggerFactory.getLogger(getClassForLogging(target));
		}
	}

	/**
	 * 确定用于日志记录的目标类
	 * @param target 目标对象
	 */
	protected Class<?> getClassForLogging(Object target) {
		return (this.hideProxyClassNames ? AopUtils.getTargetClass(target) : target.getClass());
	}

	/**
	 * 判断是否应该启动拦截器，即是否应调用invokeUnderTrace方法
	 * 默认行为检查是否启用了给定的Log实例
	 * 子类也可以重写此方法以在其他情况下应用拦截器
	 * @param invocation 追踪的方法调用器
	 * @param logger 要检查的日志实例
	 */
	protected boolean isInterceptorEnabled(MethodInvocation invocation, Logger logger) {
		return isLogEnabled(logger);
	}

	/**
	 * 判断是否启用了给定的Log实例
	 * 启用跟踪级别时，默认值为true。 子类可以覆盖此设置以更改跟踪发生的级别。
	 * @param logger 要检查的日志实例
	 */
	protected boolean isLogEnabled(Logger logger) {
		return logger.isTraceEnabled();
	}

	/**
	 * 将提供的跟踪消息写入提供的Log实例
	 */
	protected void writeToLog(Logger logger, String message) {
		writeToLog(logger, message, null);
	}

	/**
	 * 将提供的跟踪消息和Throwable写入提供的Log实例
	 * 通过invokeUnderTrace调用输入/输出结果，可能包括一个异常。注意，当setLogExceptionStackTrace为“false”时，不会记录异常的堆栈跟踪。
	 * 默认情况下，消息以TRACE级别编写。 子类可以重写此方法以控制消息的编写级别，通常还相应地重写sLogEnabled
	 */
	protected void writeToLog(Logger logger, String message, @Nullable Throwable ex) {
		if (ex != null && this.logExceptionStackTrace) {
			logger.trace(message, ex);
		}
		else {
			logger.trace(message);
		}
	}


	/**
	 * 子类必须覆盖此方法，围绕所提供的方法调用的任何跟踪。子类负责通过调用methodinvoc.proceed()来确保方法调用实际执行。
	 * 默认情况下，传入的日志实例将启用日志级别“trace”。
	 * 子类不需要再次检查这个，除非它们覆盖isInterceptorEnabled方法来修改默认行为，并且可以委托writeToLog来编写实际的消息
	 * @param logger 要写入跟踪消息的Logger
	 */
	protected abstract Object invokeUnderTrace(MethodInvocation invocation, Logger logger) throws Throwable;

}