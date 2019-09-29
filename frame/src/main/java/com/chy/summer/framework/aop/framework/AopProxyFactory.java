package com.chy.summer.framework.aop.framework;

/**
 * 由能够基于AdvisedSupport配置对象创建AOP代理的工厂接口
 * 代理应遵守以下条约：
 *
 * 他们应该实现配置包含的所有接口
 * 他们应该实现Advised接口
 * 他们应该实施equals方法来比较代理接口、advice和Target
 * 如果所有advisor和Target都可序列化，则它们应可序列化
 * 如果advisor和Target是线程安全的，则它们应该是线程安全的
 * 代理可能允许也可能不允许更改advice。如果他们不允许更改adice（例如，由于配置被冻结），则代理应在尝试更改建议时抛出AopConfigException
 */
public interface AopProxyFactory {

	/**
	 * 为给定的AOP配置创建一个AopProxy
	 */
	AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException;

}