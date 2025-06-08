package com.chy.summer.framework.beans;

import javax.annotation.Nullable;

/**
 * 提供父容器的访问功能
 */
public interface HierarchicalBeanFactory extends BeanFactory {

	/**
	 * 返回本Bean工厂的父工厂。这个方法实现了工厂的分层
	 */
	@Nullable
	BeanFactory getParentBeanFactory();

	/**
	 * 判断本地工厂是否包含这个Bean（忽略其他所有父工厂）。
	 */
	boolean containsLocalBean(String name);

}