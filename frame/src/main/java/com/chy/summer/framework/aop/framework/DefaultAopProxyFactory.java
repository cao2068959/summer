package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.aop.SummerProxy;

import java.io.Serializable;
import java.lang.reflect.Proxy;

/**
 * 默认AopProxyFactory实现，创建CGLIB代理或JDK动态代理。
 * 如果给定AdvisedSupport实例满足以下条件之一，则创建CGLIB代理 ：
 * 有optimize设置标志
 * 有proxyTargetClass设置标志
 * 没有指定代理接口
 */
public class DefaultAopProxyFactory implements AopProxyFactory, Serializable {

	/**
	 * 为给定的AOP配置创建一个AopProxy
	 */
	@Override
	public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
		//isOptimize判断是否优化，
		//isProxyTargetClass设置是否直接代理目标类，而不是仅代理特定的接口
		if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
			Class<?> targetClass = config.getTargetClass();
			if (targetClass == null) {
				throw new AopConfigException("TargetSource无法确定目标类：代理创建需要接口或目标类。");
			}
			//如果目标类，是个接口或者是个代理对象，那么依旧选在使用jdk的代理
			if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
				return new JdkDynamicAopProxy(config);
			}
			return new ObjenesisCglibAopProxy(config);
		}
		else {
			return new JdkDynamicAopProxy(config);
		}
	}

	/**
	 * 判断提供的AdvisedSupport是否仅指定了SummerProxy接口,或完全没有指定代理接口，返回true
	 */
	private boolean hasNoUserSuppliedProxyInterfaces(AdvisedSupport config) {
		//获取所有代理需要实现的接口
		Class<?>[] ifcs = config.getProxiedInterfaces();
		//判断有没有接口，或者说只有一个SummerProxy接口及其子类接口
		return (ifcs.length == 0 || (ifcs.length == 1 && SummerProxy.class.isAssignableFrom(ifcs[0])));
	}

}