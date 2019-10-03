package com.chy.summer.framework.beans.config;

import com.chy.summer.framework.beans.PropertyValues;
import com.chy.summer.framework.exception.BeansException;
import com.sun.istack.internal.Nullable;

import java.beans.PropertyDescriptor;

/**
 * 扩展了BeanPostProcessor的子接口
 * BeanPostProcessor添加了实例化之前的回调，以及在实例化之后但设置了显式属性或发生自动装配之前的回调。
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

	/**
	 * 通过构造函数或工厂方法在实例化bean之后,但在发生属性填充（通过显式属性或自动装配）之前执行操作。
	 *
	 */
	@Nullable
	default Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	/**
	 * 在实例化目标bean之前应用此BeanPostProcessor 。
	 */
	default boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		return true;
	}

	/**
	 * 在工厂将它们应用于给定bean之前，对给定的属性值进行后处理，而无需使用属性描述符。
	 */
	@Nullable
	default PropertyValues postProcessPropertyValues(
			PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {

		return pvs;
	}

}