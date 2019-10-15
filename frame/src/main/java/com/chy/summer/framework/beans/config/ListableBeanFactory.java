package com.chy.summer.framework.beans.config;

import com.chy.summer.framework.beans.BeanFactory;

public interface ListableBeanFactory extends BeanFactory {

	/**
	 * 检查此bean工厂是否包含具有给定名称的bean定义
	 * 不考虑该工厂参与的任何层次结构，并且忽略通过bean定义以外的其他方式注册的任何单例bean
	 */
	boolean containsBeanDefinition(String beanName);

}