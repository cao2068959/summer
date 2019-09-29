package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.util.Assert;

import java.util.LinkedList;
import java.util.List;

/**
 * 代理工厂的基类
 * 提供对可配置AopProxyFactory的便捷访问
 */
public class ProxyCreatorSupport extends AdvisedSupport {
	/**
	 * aop代理工厂
	 */
	private AopProxyFactory aopProxyFactory;

	private List<AdvisedSupportListener> listeners = new LinkedList<>();

	/**
	 * 创建第一个AOP代理后设置为true
	 */
	private boolean active = false;


	/**
	 * 创建一个新的ProxyCreatorSupport实例
	 */
	public ProxyCreatorSupport() {
		this.aopProxyFactory = new DefaultAopProxyFactory();
	}

	/**
	 * 使用指定的aop代理工厂创建一个新的ProxyCreatorSupport实例
	 */
	public ProxyCreatorSupport(AopProxyFactory aopProxyFactory) {
		Assert.notNull(aopProxyFactory, "AopProxyFactory不可为空");
		this.aopProxyFactory = aopProxyFactory;
	}


	/**
	 * 自定义AopProxyFactory，允许在不更改核心框架的情况下采用不同的策略
	 * 默认值为DefaultAopProxyFactory，根据要求使用动态JDK代理或CGLIB代理
	 */
	public void setAopProxyFactory(AopProxyFactory aopProxyFactory) {
		Assert.notNull(aopProxyFactory, "AopProxyFactory不可为空");
		this.aopProxyFactory = aopProxyFactory;
	}

	/**
	 * 获取此ProxyConfig使用的AopProxyFactory
	 */
	public AopProxyFactory getAopProxyFactory() {
		return this.aopProxyFactory;
	}

	/**
	 * 将给定的AdvisedSupportListener添加到此代理配置中
	 */
	public void addListener(AdvisedSupportListener listener) {
		Assert.notNull(listener, "AdvisedSupportListener不可为空");
		this.listeners.add(listener);
	}

	/**
	 * 移除给定的AdvisedSupportListener
	 */
	public void removeListener(AdvisedSupportListener listener) {
		Assert.notNull(listener, "AdvisedSupportListener不可为空");
		this.listeners.remove(listener);
	}


	/**
	 * 子类应调用此方法以获得新的AOP代理。 他们不可以使用this作为参数来创建AOP代理
	 */
	protected final synchronized AopProxy createAopProxy() {
		if (!this.active) {
			activate();
		}
		return getAopProxyFactory().createAopProxy(this);
	}

	/**
	 * 激活此代理配置
	 */
	private void activate() {
		this.active = true;
		for (AdvisedSupportListener listener : this.listeners) {
			listener.activated(this);
		}
	}

	/**
	 * 将advice更改事件传播给所有AdvisedSupportListener。
	 */
	@Override
	protected void adviceChanged() {
		super.adviceChanged();
		synchronized (this) {
			if (this.active) {
				for (AdvisedSupportListener listener : this.listeners) {
					listener.adviceChanged(this);
				}
			}
		}
	}

	/**
	 * 子类可以调用此命令来检查是否已创建任何AOP代理
	 */
	protected final synchronized boolean isActive() {
		return this.active;
	}

}
