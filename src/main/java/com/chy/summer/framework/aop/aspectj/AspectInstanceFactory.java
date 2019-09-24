package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.core.Ordered;
import com.sun.istack.internal.Nullable;

/**
 * 提供AspectJ切面的实例
 * 用于从Summer的bean工厂中解耦
 */
public interface AspectInstanceFactory extends Ordered {

	/**
	 * 创建此工厂切面的实例
	 */
	Object getAspectInstance();

	/**
	 * 暴露此工厂使用的切面类加载器
	 */
	@Nullable
	ClassLoader getAspectClassLoader();

}