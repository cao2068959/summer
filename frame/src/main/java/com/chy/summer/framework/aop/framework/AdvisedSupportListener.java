package com.chy.summer.framework.aop.framework;

/**
 * 要在ProxyCreatorSupport对象上注册的侦听器,用于接收有关激活和更改advice的回调
 */
public interface AdvisedSupportListener {

	/**
	 * 创建第一个代理时调用
	 */
	void activated(AdvisedSupport advised);

	/**
	 * 创建代理后更改advice时调用
	 */
	void adviceChanged(AdvisedSupport advised);

}