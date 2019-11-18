package com.chy.summer.framework.aop.scope;

import com.chy.summer.framework.aop.framework.AopInfrastructureBean;
import com.chy.summer.framework.aop.framework.ProxyConfig;
import com.chy.summer.framework.aop.framework.ProxyFactory;
import com.chy.summer.framework.aop.support.DelegatingIntroductionInterceptor;
import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.BeanFactoryAware;
import com.chy.summer.framework.beans.ConfigurableBeanFactory;
import com.chy.summer.framework.beans.FactoryBean;
import com.chy.summer.framework.exception.FactoryBeanNotInitializedException;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.sun.istack.internal.Nullable;

import java.lang.reflect.Modifier;

public class ScopedProxyFactoryBean extends ProxyConfig implements FactoryBean<Object>, BeanFactoryAware {

	/** 管理范围的TargetSource */
	//private final SimpleBeanTargetSource scopedTargetSource = new SimpleBeanTargetSource();

	/** 目标bean的名称 */
	@Nullable
	private String targetBeanName;

	/** 缓存的单例代理 */
	@Nullable
	private Object proxy;


	/**
	 * 创建一个新的ScopedProxyFactoryBean实例
	 */
	public ScopedProxyFactoryBean() {
		setProxyTargetClass(true);
	}


	/**
	 * 设置要作用域的Bean的名称
	 */
	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
		//this.scopedTargetSource.setTargetBeanName(targetBeanName);
	}

	/**
	 * 将拥有的工厂提供给Bean实例的回调。
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ConfigurableBeanFactory)) {
			throw new IllegalStateException("没有在ConfigurableBeanFactory中运行: " + beanFactory);
		}
		ConfigurableBeanFactory cbf = (ConfigurableBeanFactory) beanFactory;

		//this.scopedTargetSource.setBeanFactory(beanFactory);

		ProxyFactory pf = new ProxyFactory();
		pf.copyFrom(this);
		//pf.setTargetSource(this.scopedTargetSource);

		Assert.notNull(this.targetBeanName, "属性'targetBeanName' 为空");
		Class<?> beanType = beanFactory.getType(this.targetBeanName);
		if (beanType == null) {
			throw new IllegalStateException("无法为bean“ "+ this.targetBeanName +"”创建范围代理：在创建代理时无法确定目标类型。");
		}
		if (!isProxyTargetClass() || beanType.isInterface() || Modifier.isPrivate(beanType.getModifiers())) {
			pf.setInterfaces(ClassUtils.getAllInterfacesForClass(beanType, cbf.getBeanClassLoader()));
		}

		// 添加一个只实现ScopedObject上的方法的介绍
		ScopedObject scopedObject = new DefaultScopedObject(cbf, "");
		pf.addAdvice(new DelegatingIntroductionInterceptor(scopedObject));

		// 添加AopInfrastructureBean标记，以指示作用域代理本身不受自动代理
		pf.addInterface(AopInfrastructureBean.class);

		this.proxy = pf.getProxy(cbf.getBeanClassLoader());
	}


	@Override
	public Object getObject() {
		if (this.proxy == null) {
			throw new FactoryBeanNotInitializedException();
		}
		return this.proxy;
	}

	@Override
	public Class<?> getObjectType() {
		if (this.proxy != null) {
			return this.proxy.getClass();
		}
		return null;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}