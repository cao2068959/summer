package com.chy.summer.framework.aop.framework.autoProxy;

import com.chy.summer.framework.aop.framework.AbstractAdvisingBeanPostProcessor;
import com.chy.summer.framework.aop.framework.ProxyFactory;
import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.BeanFactoryAware;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import javax.annotation.Nullable;

public abstract class AbstractBeanFactoryAwareAdvisingPostProcessor extends AbstractAdvisingBeanPostProcessor
		implements BeanFactoryAware {

	@Nullable
	private ConfigurableListableBeanFactory beanFactory;

	/**
	 * 将拥有的工厂提供给Bean实例的回调。
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = (beanFactory instanceof ConfigurableListableBeanFactory ?
				(ConfigurableListableBeanFactory) beanFactory : null);
	}

	/**
	 * 获取代理工厂，并公开目标类
	 * 非目标类代理和配置的顾问的接口的默认自省将在以后应用
	 * @param bean 创建代理的bean实例
	 * @param beanName 对应的bean名称
	 */
	@Override
	protected ProxyFactory prepareProxyFactory(Object bean, String beanName) {
		if (this.beanFactory != null) {
			AutoProxyUtils.exposeTargetClass(this.beanFactory, beanName, bean.getClass());
		}

		ProxyFactory proxyFactory = super.prepareProxyFactory(bean, beanName);
		if (!proxyFactory.isProxyTargetClass() && this.beanFactory != null &&
				AutoProxyUtils.shouldProxyTargetClass(this.beanFactory, beanName)) {
			proxyFactory.setProxyTargetClass(true);
		}
		return proxyFactory;
	}

}