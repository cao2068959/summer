package com.chy.summer.framework.beans;

/**
 * 当前bean工厂使用的类加载器来加载bean类
 */
public interface BeanClassLoaderAware extends Aware {

	/**
	 * 将bean的类加载器提供给bean实例回调。
	 */
	void setBeanClassLoader(ClassLoader classLoader);

}