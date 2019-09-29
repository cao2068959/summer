package com.chy.summer.framework.aop.framework;

import com.sun.istack.internal.Nullable;

/**
 * 用于配置的AOP代理的委托接口，允许创建代理对象实例
 * DefaultAopProxyFactory可用于JDK动态代理和CGLIB代理
 */
public interface AopProxy {

	/**
	 * 创建一个新的代理对象
	 * 使用AopProxy的默认类加载器（如果需要创建代理）,通常使用的是线程上下文类加载器
	 */
	Object getProxy();

	/**
	 * 使用指定的类加载器创建一个新的代理对象
	 */
	Object getProxy(@Nullable ClassLoader classLoader);
}