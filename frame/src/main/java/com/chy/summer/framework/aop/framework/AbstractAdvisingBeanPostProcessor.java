package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.support.AopUtils;
import com.chy.summer.framework.beans.config.BeanPostProcessor;
import javax.annotation.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 将AOP Advisor应用于特定bean的BeanPostProcessor(Bean后置处理器)实现的基类
 */
public abstract class AbstractAdvisingBeanPostProcessor extends ProxyProcessorSupport implements BeanPostProcessor {

	@Nullable
	protected Advisor advisor;

	/**
	 * 设置遇到预先advised的对象时，是否应将此后处理的advisor应用于现有advisor之前
	 */
	protected boolean beforeExistingAdvisors = false;

	/**
	 * 类是否有可以与此后置处理器的Advisor关联结果的缓存
	 */
	private final Map<Class<?>, Boolean> eligibleBeans = new ConcurrentHashMap<>(256);


	/**
	 * 设置遇到预先advised的对象时，是否应将此后处理的advisor应用于现有advisor之前
	 * 默认值为“ false”，在现有advisor之后应用advisor，即尽可能接近目标方法。
	 * 将此设置为“ true”，该后处理器的advisor也包装现有的advisor。
	 */
	public void setBeforeExistingAdvisors(boolean beforeExistingAdvisors) {
		this.beforeExistingAdvisors = beforeExistingAdvisors;
	}

	/**
	 * 初始化前 执行
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		return bean;
	}

	/**
	 * 初始化后执行
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		if (bean instanceof AopInfrastructureBean || this.advisor == null) {
			// AopInfrastructureBean类型的bean不会被自动代理
			return bean;
		}

		if (bean instanceof Advised) {
			Advised advised = (Advised) bean;
			//配置没有冻结，并且后置处理器也可以应用于目标类
			if (!advised.isFrozen() && isEligible(AopUtils.getTargetClass(bean))) {
				//本地Advisor添加到现有代理的Advisor链中
				if (this.beforeExistingAdvisors) {
					//加在链头
					advised.addAdvisor(0, this.advisor);
				}
				else {
					//加在链尾
					advised.addAdvisor(this.advisor);
				}
				return bean;
			}
		}

		if (isEligible(bean, beanName)) {
			//获取代理工厂
			ProxyFactory proxyFactory = prepareProxyFactory(bean, beanName);
			if (!proxyFactory.isProxyTargetClass()) {
				//评估代理接口,检查给定bean类上的接口，并在适当时它们会应用于ProxyFactory
				evaluateProxyInterfaces(bean.getClass(), proxyFactory);
			}
			proxyFactory.addAdvisor(this.advisor);
			//子类的扩展口
			customizeProxyFactory(proxyFactory);
			return proxyFactory.getProxy(getProxyClassLoader());
		}

		//无需异步代理
		return bean;
	}

	/**
	 * 检查给定的bean是否有资格向这个后置处理器的Advisor提供advise
	 * 委托给方法isEligible(Class<?> targetClass)进行类型检查
	 * 允许被重写，例如，通过名称明确地排除某些bean
	 * @param bean bean的实例
	 * @param beanName bean的名称
	 */
	protected boolean isEligible(Object bean, String beanName) {
		return isEligible(bean.getClass());
	}

	/**
	 * 检查给定的类型是否有可以与此后置处理器的Advisor关联
	 * 对每个bean目标类实现canApply结果的缓存
	 * @param targetClass 需要检查的类型
	 */
	protected boolean isEligible(Class<?> targetClass) {
		//尝试从缓存中获取
		Boolean eligible = this.eligibleBeans.get(targetClass);
		if (eligible != null) {
			return eligible;
		}
		if (this.advisor == null) {
			return false;
		}
		//给定的advisor能否应用于给定的类
		eligible = AopUtils.canApply(this.advisor, targetClass);
		this.eligibleBeans.put(targetClass, eligible);
		return eligible;
	}

	/**
	 * 获取代理工厂
	 * 子类可以自定义目标实例的处理，特别是目标类的公开
	 * 非目标类代理和配置的顾问的接口的默认自省将在以后应用
	 * customizeProxyFactory允许在代理创建之前对这些部件进行后期定制
	 * @param bean 创建代理的bean实例
	 * @param beanName 对应的bean名称
	 */
	protected ProxyFactory prepareProxyFactory(Object bean, String beanName) {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.copyFrom(this);
		proxyFactory.setTarget(bean);
		return proxyFactory;
	}

	/**
	 * 初始化之后的最终执行
	 * 子类可以选择实现此目的：例如，更改公开的接口
	 */
	protected void customizeProxyFactory(ProxyFactory proxyFactory) {
	}

}