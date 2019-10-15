package com.chy.summer.framework.aop.aspectj.annotation;

import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.ConfigurableBeanFactory;
import com.chy.summer.framework.core.ordered.OrderUtils;
import com.chy.summer.framework.core.ordered.Ordered;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.sun.istack.internal.Nullable;

import java.io.Serializable;

public class BeanFactoryAspectInstanceFactory implements MetadataAwareAspectInstanceFactory, Serializable {

	private final BeanFactory beanFactory;

	/**
	 * bean的名称
	 */
	private final String name;

	private final AspectMetadata aspectMetadata;


	/**
	 * 创建一个BeanFactoryAspectInstanceFactory
	 * 将调用AspectJ进行内部检查，使用从BeanFactory返回的bean名称和返回的类型来创建AJType元数据。
	 * @param beanFactory 用于获取实例的BeanFactory
	 * @param name bean的名称
	 */
	public BeanFactoryAspectInstanceFactory(BeanFactory beanFactory, String name) {
		this(beanFactory, name, null);
	}

	/**
	 * 创建一个BeanFactoryAspectInstanceFactory，提供AspectJ内省创建AJType元数据的类型。如果BeanFactory可以将类型视为子类（如使用CGLIB时），并且信息应与超类有关，则使用此方法。
	 * @param name bean的名称
	 * @param type 需要AspectJ内省的类型
	 */
	public BeanFactoryAspectInstanceFactory(BeanFactory beanFactory, String name, @Nullable Class<?> type) {
		Assert.notNull(beanFactory, "BeanFactory不可为空");
		Assert.notNull(name, "Bean的名称不可为空");
		this.beanFactory = beanFactory;
		this.name = name;
		Class<?> resolvedType = type;
		if (type == null) {
			resolvedType = beanFactory.getType(name);
			Assert.notNull(resolvedType, "无法解析的Bean类型 - 明确指定切面类");
		}
		this.aspectMetadata = new AspectMetadata(resolvedType, name);
	}

	/**
	 * 创建此工厂切面的实例
	 */
	@Override
	public Object getAspectInstance() {
		return this.beanFactory.getBean(this.name);
	}

	/**
	 * 暴露此工厂使用的切面类加载器
	 */
	@Override
	@Nullable
	public ClassLoader getAspectClassLoader() {
		return (this.beanFactory instanceof ConfigurableBeanFactory ?
				((ConfigurableBeanFactory) this.beanFactory).getBeanClassLoader() :
				ClassUtils.getDefaultClassLoader());
	}

	/**
	 * 获取这个工厂的aspect的AspectJ AspectMetadata。
	 * @return aspect的元数据
	 */
	@Override
	public AspectMetadata getAspectMetadata() {
		return this.aspectMetadata;
	}

	/**
	 * 获取对象对应的锁,用于初始化的时候锁定对象
	 */
	@Override
	@Nullable
	public Object getAspectCreationMutex() {
		if (this.beanFactory.isSingleton(this.name)) {
			return null;
		}
//		else if (this.beanFactory instanceof ConfigurableBeanFactory) {
//			return ((ConfigurableBeanFactory) this.beanFactory).getSingletonMutex();
//		}
		else {
			return this;
		}
	}

	/**
	 * 获取该工厂目标切面的顺序，可以是通过实现Ordered接口表达的特定于实例的顺序 （仅检查单例bean），
	 * 或者是通过Order类级别的注释表达的顺序
	 */
	@Override
	public int getOrder() {
		//通过bean的名称获取bean的类型
		Class<?> type = this.beanFactory.getType(this.name);
		if (type != null) {
			if (Ordered.class.isAssignableFrom(type) && this.beanFactory.isSingleton(this.name)) {
				return ((Ordered) this.beanFactory.getBean(this.name)).getOrder();
			}
			return OrderUtils.getOrder(type, Ordered.LOWEST_PRECEDENCE);
		}
		return Ordered.LOWEST_PRECEDENCE;
	}


	@Override
	public String toString() {
		return getClass().getSimpleName() + ": bean name '" + this.name + "'";
	}

}