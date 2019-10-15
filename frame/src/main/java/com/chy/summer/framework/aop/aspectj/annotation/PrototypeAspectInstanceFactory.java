package com.chy.summer.framework.aop.aspectj.annotation;

import com.chy.summer.framework.beans.BeanFactory;

import java.io.Serializable;

public class PrototypeAspectInstanceFactory extends BeanFactoryAspectInstanceFactory implements Serializable {

	/**
	 * 创建一个PrototypeAspectInstanceFactory。
	 * 将调用AspectJ进行内部检查，使用从BeanFactory返回的给定bean名称和返回的类型来创建AJType元数据。
	 */
	public PrototypeAspectInstanceFactory(BeanFactory beanFactory, String name) {
		super(beanFactory, name);
		if (beanFactory.isSingleton(name)) {
			throw new IllegalArgumentException(
					"无法将PrototypeAspectInstanceFactory与名为'" + name + "'的Bean一起使用: 不是原型");
		}
	}

}
