package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.core.NamedThreadLocal;
import com.sun.istack.internal.Nullable;

/**
 * 自动代理创建器（例如AbstractAdvisorAutoProxyCreator）
 * 当前代理创建上下文的持有对象。
 */
public class ProxyCreationContext {

	/**
	 * 在Advisor匹配期间，ThreadLocal保存当前代理的bean名称
	 */
	private static final ThreadLocal<String> currentProxiedBeanName =
			new NamedThreadLocal<>("当前代理bean的名称");


	/**
	 * 获取当前代理的bean实例的名称。
	 */
	@Nullable
	public static String getCurrentProxiedBeanName() {
		return currentProxiedBeanName.get();
	}

	/**
	 * 设置当前代理的bean实例的名称,传入null则清除ThreadLocal
	 */
	static void setCurrentProxiedBeanName(@Nullable String beanName) {
		if (beanName != null) {
			//设置beanName
			currentProxiedBeanName.set(beanName);
		}
		else {
			//清除ThreadLocal
			currentProxiedBeanName.remove();
		}
	}

}