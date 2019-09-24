package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.beans.Aware;
import com.chy.summer.framework.beans.BeanFactory;

/**
 * bean实现这个借口可以获取到自己的beanfactory
 */
public interface BeanFactoryAware extends Aware {

	/**
	 * 将拥有的工厂提供给Bean实例的回调。
	 */
	void setBeanFactory(BeanFactory beanFactory);

}