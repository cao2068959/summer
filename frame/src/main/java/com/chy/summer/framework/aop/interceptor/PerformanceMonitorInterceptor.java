package com.chy.summer.framework.aop.interceptor;

import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.chy.summer.framework.util.core.StopWatch;
import org.slf4j.Logger;

/**
 * 简单的AOP Alliance MethodInterceptor用于性能监视。 该拦截器对被拦截的方法调用没有影响
 */
public class PerformanceMonitorInterceptor extends AbstractMonitoringInterceptor {

	/**
	 * 使用静态记录器创建一个新的PerformanceMonitorInterceptor
	 */
	public PerformanceMonitorInterceptor() {
	}

	/**
	 * 根据给定的标志，使用动态或静态记录器创建一个新的PerformanceMonitorInterceptor。
	 * @param useDynamicLogger 使用动态记录器还是静态记录器
	 */
	public PerformanceMonitorInterceptor(boolean useDynamicLogger) {
		setUseDynamicLogger(useDynamicLogger);
	}

	/**
	 * 添加计时器的拦截调用
	 * @param invocation 需要执行的方法拦截器
	 * @param logger 要写入跟踪消息的Logger
	 */
	@Override
	protected Object invokeUnderTrace(MethodInvocation invocation, Logger logger) throws Throwable {
		String name = createInvocationTraceName(invocation);
		StopWatch stopWatch = new StopWatch(name);
		stopWatch.start(name);
		try {
			return invocation.proceed();
		}
		finally {
			stopWatch.stop();
			writeToLog(logger, stopWatch.shortSummary());
		}
	}

}