package com.chy.summer.framework.aop.framework.adapter;

/**
 * 统一的advisor适配器注册表，单例的
 */
public abstract class GlobalAdvisorAdapterRegistry {

	/**
	 * 跟踪单个实例，以便我们可以将其返回给请求它的类。
	 */
	private static AdvisorAdapterRegistry instance = new DefaultAdvisorAdapterRegistry();

	/**
	 * 获取这个DefaultAdvisorAdapterRegistry单例实例
	 */
	public static AdvisorAdapterRegistry getInstance() {
		return instance;
	}

	/**
	 * 重置这个DefaultAdvisorAdapterRegistry，删除掉所有的已注册的适配器
	 */
	static void reset() {
		instance = new DefaultAdvisorAdapterRegistry();
	}

}