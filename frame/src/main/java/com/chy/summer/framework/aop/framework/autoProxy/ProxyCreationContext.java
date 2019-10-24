package com.chy.summer.framework.aop.framework.autoProxy;

import com.chy.summer.framework.core.NamedThreadLocal;
import com.sun.istack.internal.Nullable;

/**
 * 自动代理创建者公开的当前代理创建上下文的持有者
 */
public class ProxyCreationContext {

	/** 在Advisor匹配期间，ThreadLocal保存当前代理的bean名称 */
	private static final ThreadLocal<String> currentProxiedBeanName =
			new NamedThreadLocal<>("当前代理bean的名称");


	/**
	 * 返回当前代理的bean实例的名称
	 */
	@Nullable
	public static String getCurrentProxiedBeanName() {
		return currentProxiedBeanName.get();
	}

	/**
	 * 设置当前代理的bean实例的名称
	 * @param beanName bean的名称，设置null重置它
	 */
	static void setCurrentProxiedBeanName(@Nullable String beanName) {
		if (beanName != null) {
			currentProxiedBeanName.set(beanName);
		}
		else {
			currentProxiedBeanName.remove();
		}
	}

}