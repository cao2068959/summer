package com.chy.summer.framework.aop.interceptor;

import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.jamonapi.MonKey;
import com.jamonapi.MonKeyImp;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import com.jamonapi.utils.Misc;
import org.slf4j.Logger;

/**
 * Jamon性能监控器拦截器
 *
 * 在大多数情况下都可以使用的静态MonitorFactory。 在需要其他工厂的情况下，可以直接实例化FactoryEnabled。
 * 注意，这主要是针对FactoryEnabled和FactoryDisabled的包装器。您还可以通过调用getFactory()来获得底层工厂
 */
public class JamonPerformanceMonitorInterceptor extends AbstractMonitoringInterceptor {

	private boolean trackAllInvocations = false;


	/**
	 * 使用静态记录器创建一个新的JamonPerformanceMonitorInterceptor
	 */
	public JamonPerformanceMonitorInterceptor() {
	}

	/**
	 * 根据给定的标志，使用动态或静态记录器创建一个新的JamonPerformanceMonitorInterceptor。
	 * @param useDynamicLogger 使用动态记录器还是静态记录器
	 */
	public JamonPerformanceMonitorInterceptor(boolean useDynamicLogger) {
		setUseDynamicLogger(useDynamicLogger);
	}

	/**
	 * 根据给定的标志，使用动态或静态记录器创建一个新的JamonPerformanceMonitorInterceptor
	 * @param useDynamicLogger 使用动态记录器还是静态记录器
	 * @param trackAllInvocations 是否跟踪通过此拦截器的所有调用，还是仅跟踪启用了跟踪日志记录的调用
	 */
	public JamonPerformanceMonitorInterceptor(boolean useDynamicLogger, boolean trackAllInvocations) {
		setUseDynamicLogger(useDynamicLogger);
		setTrackAllInvocations(trackAllInvocations);
	}


	/**
	 * 设置是跟踪所有通过此拦截器的调用，还是仅跟踪启用了跟踪日志记录的调用
	 * 默认值是“false”:只有启用跟踪日志记录的调用才会被监视。
	 * 指定“true”让JAMon跟踪所有调用，即使在禁用跟踪日志记录时也收集统计信息。
	 */
	public void setTrackAllInvocations(boolean trackAllInvocations) {
		this.trackAllInvocations = trackAllInvocations;
	}


	/**
	 * 是否启用拦截器
	 */
	@Override
	protected boolean isInterceptorEnabled(MethodInvocation invocation, Logger logger) {
		return (this.trackAllInvocations || isLogEnabled(logger));
	}

	/**
	 * 用JAMon Monitor包裹调用，并将当前性能统计信息写入日志（如果已启用）
	 */
	@Override
	protected Object invokeUnderTrace(MethodInvocation invocation, Logger logger) throws Throwable {
		String name = createInvocationTraceName(invocation);
		MonKey key = new MonKeyImp(name, name, "ms.");

		Monitor monitor = MonitorFactory.start(key);
		try {
			return invocation.proceed();
		}
		catch (Throwable ex) {
			trackException(key, ex);
			throw ex;
		}
		finally {
			monitor.stop();
			if (!this.trackAllInvocations || isLogEnabled(logger)) {
				writeToLog(logger, "方法[" + name + "]的JAMon性能统计:\n" + monitor);
			}
		}
	}

	/**
	 * 计算抛出的异常并将堆栈跟踪放入键的详细信息部分。
	 * 这将允许在JAMon web应用程序中查看堆栈跟踪。
	 */
	protected void trackException(MonKey key, Throwable ex) {
		String stackTrace = "堆栈跟踪=" + Misc.getExceptionTrace(ex);
		key.setDetails(stackTrace);

		// 具体的异常计数器
		MonitorFactory.add(new MonKeyImp(ex.getClass().getName(), stackTrace, "Exception"), 1);

		// 通用异常计数器，所有抛出的异常的总数
		MonitorFactory.add(new MonKeyImp(MonitorFactory.EXCEPTIONS_LABEL, stackTrace, "Exception"), 1);
	}

}