package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.aop.TargetSource;
import com.chy.summer.framework.aop.framework.adapter.AdvisorAdapterRegistry;
import com.chy.summer.framework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import com.chy.summer.framework.aop.target.SingletonTargetSource;
import com.chy.summer.framework.beans.BeanClassLoaderAware;
import com.chy.summer.framework.beans.FactoryBean;
import com.chy.summer.framework.beans.InitializingBean;
import com.chy.summer.framework.exception.FactoryBeanNotInitializedException;
import com.chy.summer.framework.util.ClassUtils;
import javax.annotation.Nullable;

/**
 * 便捷的FactoryBean类型的超类，用于生成单例作用域的代理对象
 */
public abstract class AbstractSingletonProxyFactoryBean extends ProxyConfig
		implements FactoryBean<Object>, BeanClassLoaderAware, InitializingBean {

	@Nullable
	private Object target;

	@Nullable
	private Class<?>[] proxyInterfaces;

	@Nullable
	private Object[] preInterceptors;

	@Nullable
	private Object[] postInterceptors;

	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

	@Nullable
	private transient ClassLoader proxyClassLoader;

	@Nullable
	private Object proxy;


	/**
	 * 设置目标对象，即使用事务代理包装的bean
	 */
	public void setTarget(Object target) {
		this.target = target;
	}

	/**
	 * 指定要代理的接口列表
	 * 如果未指定（默认值），则AOP基础结构通过分析目标(代理目标对象实现的所有接口)来确定哪些接口需要代理
	 */
	public void setProxyInterfaces(Class<?>[] proxyInterfaces) {
		this.proxyInterfaces = proxyInterfaces;
	}

	/**
	 * 在隐式事务拦截器之前设置附加的拦截器(或advisor)，例如PerformanceMonitorInterceptor。
	 */
	public void setPreInterceptors(Object[] preInterceptors) {
		this.preInterceptors = preInterceptors;
	}

	/**
	 * 在隐式事务拦截器之后设置附加的拦截器(或advisor)
	 */
	public void setPostInterceptors(Object[] postInterceptors) {
		this.postInterceptors = postInterceptors;
	}

	/**
	 * 指定要使用的AdvisorAdapterRegistry
	 * 默认使用通用的AdvisorAdapterRegistry.
	 */
	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
		this.advisorAdapterRegistry = advisorAdapterRegistry;
	}

	/**
	 * 设置ClassLoader以生成代理类
	 */
	public void setProxyClassLoader(ClassLoader classLoader) {
		this.proxyClassLoader = classLoader;
	}

	/**
	 * 将bean的类加载器提供给bean实例回调。
	 */
	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		if (this.proxyClassLoader == null) {
			this.proxyClassLoader = classLoader;
		}
	}

	/**
	 * 当BeanFactory设置了所提供的所有bean属性(并满足BeanFactoryAware和applicationcontextAware)之后调用
	 * 此方法仅在设置了所有bean属性后才允许bean实例执行初始化，并在配置错误的情况下抛出异常
	 */
	@Override
	public void afterPropertiesSet() {
		if (this.target == null) {
			throw new IllegalArgumentException("必须提供属性“target”");
		}
		if (this.target instanceof String) {
			throw new IllegalArgumentException("'target'必须是一个bean引用，而不是一个bean名称作为值");
		}
		if (this.proxyClassLoader == null) {
			this.proxyClassLoader = ClassUtils.getDefaultClassLoader();
		}

		ProxyFactory proxyFactory = new ProxyFactory();

		//添加前拦截器
		if (this.preInterceptors != null) {
			for (Object interceptor : this.preInterceptors) {
				proxyFactory.addAdvisor(this.advisorAdapterRegistry.wrap(interceptor));
			}
		}

		//添加主拦截器（通常是Advisor）
		proxyFactory.addAdvisor(this.advisorAdapterRegistry.wrap(createMainInterceptor()));

		//添加后拦截器
		if (this.postInterceptors != null) {
			for (Object interceptor : this.postInterceptors) {
				proxyFactory.addAdvisor(this.advisorAdapterRegistry.wrap(interceptor));
			}
		}

		//代理工厂复制配置
		proxyFactory.copyFrom(this);
		//确定TargetSource
		TargetSource targetSource = createTargetSource(this.target);
		proxyFactory.setTargetSource(targetSource);

		if (this.proxyInterfaces != null) {
			proxyFactory.setInterfaces(this.proxyInterfaces);
		}
		else if (!isProxyTargetClass()) {
			Class<?> targetClass = targetSource.getTargetClass();
			if (targetClass != null) {
				proxyFactory.setInterfaces(ClassUtils.getAllInterfacesForClass(targetClass, this.proxyClassLoader));
			}
		}
		//需要子类实现的后置方法
		postProcessProxyFactory(proxyFactory);

		this.proxy = proxyFactory.getProxy(this.proxyClassLoader);
	}

	/**
	 * 为给定的目标(或目标源)确定一个TargetSource
	 * @param target 如果这是TargetSource的实现，它将用作此对象的TargetSource;否则它被包装在一个SingletonTargetSource中
	 */
	protected TargetSource createTargetSource(Object target) {
		if (target instanceof TargetSource) {
			return (TargetSource) target;
		}
		else {
			return new SingletonTargetSource(target);
		}
	}

	/**
	 * 子类的钩子，用于在使用它创建代理实例之前对ProxyFactory进行后置处理。
	 * @param proxyFactory 将要使用的AOP ProxyFactory
	 */
	protected void postProcessProxyFactory(ProxyFactory proxyFactory) {
	}


	@Override
	public Object getObject() {
		if (this.proxy == null) {
			throw new FactoryBeanNotInitializedException();
		}
		return this.proxy;
	}

	@Override
	@Nullable
	public Class<?> getObjectType() {
		if (this.proxy != null) {
			return this.proxy.getClass();
		}
		if (this.proxyInterfaces != null && this.proxyInterfaces.length == 1) {
			return this.proxyInterfaces[0];
		}
		if (this.target instanceof TargetSource) {
			return ((TargetSource) this.target).getTargetClass();
		}
		if (this.target != null) {
			return this.target.getClass();
		}
		return null;
	}

	@Override
	public final boolean isSingleton() {
		return true;
	}


	/**
	 * 为此代理工厂bean创建主拦截器
	 * 通常是advisor，但也可以是任何类型的advice
	 * 前置拦截器将在此拦截器之前应用，后置拦截器将在此拦截器之后应用。
	 */
	protected abstract Object createMainInterceptor();

}