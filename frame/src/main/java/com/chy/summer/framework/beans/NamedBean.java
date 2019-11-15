package com.chy.summer.framework.beans;

/**
 * 返回对象的bean名称
 *
 * 可以引入该接口以避免在与IoC和AOP一起使用的对象中对bean名称的脆弱依赖
 */
public interface NamedBean {

	/**
	 * bean工厂中返回该bean的名称
	 */
	String getBeanName();

}