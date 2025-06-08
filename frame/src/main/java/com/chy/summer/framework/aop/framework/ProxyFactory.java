package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.aop.TargetSource;
import com.chy.summer.framework.aop.aopalliance.intercept.Interceptor;
import com.chy.summer.framework.util.ClassUtils;
import javax.annotation.Nullable;

/**
 * 用于AOP代理的工厂，以编程的方式使用，而不是通过bean工厂中的声明设置
 * 此类提供了一种在自定义用户代码中获取和配置AOP代理实例的简单方式
 */
public class ProxyFactory extends ProxyCreatorSupport {

	/**
	 * 创建一个代理工厂
	 */
	public ProxyFactory() {
	}

	/**
	 * 创建一个新的代理工厂
	 * 将代理给定目标实现的所有接口
	 */
	public ProxyFactory(Object target) {
		//设置目标，用目标创建目标源
		setTarget(target);
		//设置这个对象所有实现的接口
		setInterfaces(ClassUtils.getAllInterfaces(target));
	}

	/**
	 * 创建一个新的代理工厂
	 * 没有目标，只有接口。 必须添加拦截器
	 */
	public ProxyFactory(Class<?>... proxyInterfaces) {
		//设置需要实现的接口
		setInterfaces(proxyInterfaces);
	}

	/**
	 * 为给定的接口和拦截器创建一个新的ProxyFactory。
	 * 用于为单个拦截器创建代理
	 * @param proxyInterface 代理应实现的接口
	 * @param interceptor 代理应调用的拦截器
	 */
	public ProxyFactory(Class<?> proxyInterface, Interceptor interceptor) {
		addInterface(proxyInterface);
		addAdvice(interceptor);
	}

	/**
	 * 为指定的TargetSource创建一个ProxyFactory，使代理实现指定的接口。
	 */
	public ProxyFactory(Class<?> proxyInterface, TargetSource targetSource) {
		addInterface(proxyInterface);
		setTargetSource(targetSource);
	}


	/**
	 * 根据此工厂中的设置创建一个新的代理
	 * 可以反复调用。如果我们添加或删除了接口，每次返回的结果会不同。可以添加和删除拦截器
	 *
	 * 使用默认的类加载器
	 */
	public Object getProxy() {
		return createAopProxy().getProxy();
	}

	/**
	 * 根据给定的类加载器创建一个新的代理
	 */
	public Object getProxy(@Nullable ClassLoader classLoader) {
		return createAopProxy().getProxy(classLoader);
	}


	/**
	 * 为给定的接口和拦截器创建一个新的代理
	 */
	public static <T> T getProxy(Class<T> proxyInterface, Interceptor interceptor) {
		return (T) new ProxyFactory(proxyInterface, interceptor).getProxy();
	}

	/**
	 * 为指定的TargetSource创建代理，实现指定的接口。
	 */
	public static <T> T getProxy(Class<T> proxyInterface, TargetSource targetSource) {
		return (T) new ProxyFactory(proxyInterface, targetSource).getProxy();
	}

	/**
	 * 为指定的TargetSource创建代理，同于扩展的目标类
	 */
	public static Object getProxy(TargetSource targetSource) {
		if (targetSource.getTargetClass() == null) {
			throw new IllegalArgumentException("无法使用空类型的TargetSource创建类代理");
		}
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTargetSource(targetSource);
		proxyFactory.setProxyTargetClass(true);
		return proxyFactory.getProxy();
	}

}