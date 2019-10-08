package com.chy.summer.framework.aop.framework.autoProxy;

import com.chy.summer.framework.aop.TargetSource;

/**
 * 目标源创建器，用于创建特殊的目标源
 */
public interface TargetSourceCreator {

	/**
	 * 为给定的bean创建一个特殊的TargetSource
	 * @param beanClass 用于创建TargetSource的bean的类
	 * @param beanName bean名称
	 */
	TargetSource getTargetSource(Class<?> beanClass, String beanName);

}