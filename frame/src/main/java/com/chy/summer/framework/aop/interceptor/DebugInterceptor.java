package com.chy.summer.framework.aop.interceptor;

import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;

/**
 * 可以在一个链中引入AOP Alliance MethodInterceptor来显示关于被拦截的调用的详细信息
 *
 * 记录方法调用前和方法调用后的完整调用细节，包括调用参数和调用计数。
 * 这仅用于调试使用;使用SimpleTraceInterceptor或CustomizableTraceInterceptor进行纯跟踪
 */
public class DebugInterceptor extends SimpleTraceInterceptor {

	/**
	 * 这个拦截器的调用次数
	 */
	private volatile long count;


	/**
	 * 使用静态日志程序创建一个新的DebugInterceptor
	 */
	public DebugInterceptor() {
	}

	/**
	 * 根据给定的标志，使用动态或静态记录器创建一个新的DebugInterceptor
	 * @param useDynamicLogger 使用动态记录器还是静态记录器
	 */
	public DebugInterceptor(boolean useDynamicLogger) {
		setUseDynamicLogger(useDynamicLogger);
	}


	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		synchronized (this) {
			this.count++;
		}
		return super.invoke(invocation);
	}

	/**
	 * 返回给定方法调用的描述
	 * @param invocation 调用的方法
	 */
	@Override
	protected String getInvocationDescription(MethodInvocation invocation) {
		return invocation + "; 已拦截次数=" + this.count;
	}


	/**
	 * 返回此拦截器已被调用的次数
	 */
	public long getCount() {
		return this.count;
	}

	/**
	 * 将调用计数重置为零
	 */
	public synchronized void resetCount() {
		this.count = 0;
	}

}