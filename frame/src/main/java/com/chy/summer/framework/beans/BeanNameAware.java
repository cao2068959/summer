package com.chy.summer.framework.beans;

/**
 * 想要在Bean工厂中知道其Bean名称的Bean将实现的接口
 */
public interface BeanNameAware extends Aware {

	/**
	 * 在创建此bean的bean工厂中设置bean的名称
	 */
	void setBeanName(String name);

}