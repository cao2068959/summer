package com.chy.summer.framework.aop.framework.adapter;

import com.chy.summer.framework.beans.config.BeanPostProcessor;
import com.chy.summer.framework.exception.BeansException;

/**
 * 为容器中管理的Bean注册一个面向切面编程的通知适配器
 */
public class AdvisorAdapterRegistrationManager implements BeanPostProcessor {

	/**
	 * 容器中负责管理切面通知适配器注册的对象
	 */
	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();


	/**
	 * 指定AdvisorAdapterRegistry来注册AdvisorAdapter bean，默认值为全局AdvisorAdapterRegistry
	 * @see GlobalAdvisorAdapterRegistry
	 */
	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
		//如果容器创建的Bean实例对象是一个切面通知适配器，则向容器的注册
		this.advisorAdapterRegistry = advisorAdapterRegistry;
	}


	/**
	 * BeanPostProcessor在Bean对象初始化后的操作
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
	 * BeanPostProcessor在Bean对象初始化前的操作
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof AdvisorAdapter){
			this.advisorAdapterRegistry.registerAdvisorAdapter((AdvisorAdapter) bean);
		}
		//没有做任何操作，直接返回容器创建的Bean对象
		return bean;
	}

}