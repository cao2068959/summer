package com.chy.summer.framework.aop.scope;

import com.chy.summer.framework.beans.ConfigurableBeanFactory;
import com.chy.summer.framework.util.Assert;

import java.io.Serializable;

public class DefaultScopedObject implements ScopedObject, Serializable {

	private final ConfigurableBeanFactory beanFactory;

	private final String targetBeanName;


	/**
	 * 创建DefaultScopedObject类的新实例
	 * @param beanFactory 拥有范围目标对象的ConfigurableBeanFactory
	 * @param targetBeanName 目标bean的名称
	 */
	public DefaultScopedObject(ConfigurableBeanFactory beanFactory, String targetBeanName) {
		Assert.notNull(beanFactory, "BeanFactory不可为空");
		Assert.hasText(targetBeanName, "'targetBeanName'不可为空");
		this.beanFactory = beanFactory;
		this.targetBeanName = targetBeanName;
	}


	@Override
	public Object getTargetObject() {
		return this.beanFactory.getBean(this.targetBeanName);
	}

	@Override
	public void removeFromScope() {
		//TODO GYX 差一个方法
//		this.beanFactory.destroyScopedBean(this.targetBeanName);
	}

}