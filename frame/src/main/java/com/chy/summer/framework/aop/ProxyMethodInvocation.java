package com.chy.summer.framework.aop;

import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import javax.annotation.Nullable;

/**
 * 允许访问通过其方法调用的代理
 */
public interface ProxyMethodInvocation extends MethodInvocation {

	/**
	 * 获取此方法调用的代理
	 */
	Object getProxy();

	/**
	 * 创建此对象的克隆
	 */
	MethodInvocation invocableClone();

	/**
	 * 创建此对象的副本
	 */
	MethodInvocation invocableClone(Object... arguments);

	/**
	 * 在该链中的任何advice中设置要在后续调用中使用的参数
	 */
	void setArguments(Object... arguments);

	/**
	 * 将给定值的添加到指定属性
	 * @param key 属性名称
	 * @param value 属性的值，或null重置它
	 */
	void setUserAttribute(String key, @Nullable Object value);

	/**
	 * 获取指定属性的值
	 * @param key 属性名称
	 */
	@Nullable
	Object getUserAttribute(String key);

}