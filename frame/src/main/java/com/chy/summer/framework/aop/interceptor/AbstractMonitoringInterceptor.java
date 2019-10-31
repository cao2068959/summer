package com.chy.summer.framework.aop.interceptor;

import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.sun.istack.internal.Nullable;

import java.lang.reflect.Method;

/**
 * 用于监视截取程序(如性能监视器)的基类。
 * 提供可配置的“前缀和后缀”属性，帮助对性能监视结果进行分类/分组
 * 在invokeUnderTrace实现中，子类应该调用createInvocationTraceName方法来为给定的跟踪创建一个名称，包括关于方法调用的信息以及前缀/后缀
 */
public abstract class AbstractMonitoringInterceptor extends AbstractTraceInterceptor {

	private String prefix = "";

	private String suffix = "";

	private boolean logTargetClassInvocation = false;


	/**
	 * 设置附加到跟踪数据的文本
	 * 默认为空
	 */
	public void setPrefix(@Nullable String prefix) {
		this.prefix = (prefix != null ? prefix : "");
	}

	/**
	 * 获取附加的前缀
	 */
	protected String getPrefix() {
		return this.prefix;
	}

	/**
	 * 设置附加的后缀
	 * <p>Default is none.
	 */
	public void setSuffix(@Nullable String suffix) {
		this.suffix = (suffix != null ? suffix : "");
	}

	/**
	 * 获取附加的后缀
	 */
	protected String getSuffix() {
		return this.suffix;
	}

	/**
	 * 设置是否在目标类上记录调用
	 * 默认值为“ false”，根据代理接口/类名称记录调用。
	 */
	public void setLogTargetClassInvocation(boolean logTargetClassInvocation) {
		this.logTargetClassInvocation = logTargetClassInvocation;
	}


	/**
	 * 为给定的方法调用创建一个可用于跟踪/日志目的的字符串名称
	 * 此名称由已配置的前缀、所调用方法的完全限定名和已配置的后缀组成
	 */
	protected String createInvocationTraceName(MethodInvocation invocation) {
		StringBuilder sb = new StringBuilder(getPrefix());
		Method method = invocation.getMethod();
		Class<?> clazz = method.getDeclaringClass();
		if (this.logTargetClassInvocation && clazz.isInstance(invocation.getThis())) {
			clazz = invocation.getThis().getClass();
		}
		sb.append(clazz.getName());
		sb.append('.').append(method.getName());
		sb.append(getSuffix());
		return sb.toString();
	}

}