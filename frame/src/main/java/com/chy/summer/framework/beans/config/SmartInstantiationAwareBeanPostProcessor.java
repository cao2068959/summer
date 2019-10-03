package com.chy.summer.framework.beans.config;

import com.chy.summer.framework.exception.BeansException;
import com.sun.istack.internal.Nullable;

import java.lang.reflect.Constructor;

/**
 * InstantiationAwareBeanPostProcessor接口的扩展，添加了用于预测已处理bean的最终类型的回调
 */
public interface SmartInstantiationAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessor {

	/**
	 * 预测Bean的类型，返回第一个预测成功的Class类型，如果不能预测返回null
	 */
	@Nullable
	default Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	/**
	 * 选择合适的构造器，比如目标对象有多个构造器，在这里可以进行一些定制化，选择合适的构造器
	 * beanClass参数表示目标实例的类型，beanName是目标实例在容器中的name
	 * 返回值是个构造器数组，如果返回null，会执行下一个PostProcessor的determineCandidateConstructors方法；否则选取该PostProcessor选择的构造器
	 */
	@Nullable
	default Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName)
			throws BeansException {

		return null;
	}

	/**
	 * 获得提前暴露的bean引用。主要用于解决循环引用的问题
	 * 只有单例对象才会调用此方法
	 */
	default Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
		return bean;
	}

}