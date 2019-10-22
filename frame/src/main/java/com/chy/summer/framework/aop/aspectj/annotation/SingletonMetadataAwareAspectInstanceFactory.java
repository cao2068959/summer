package com.chy.summer.framework.aop.aspectj.annotation;

import com.chy.summer.framework.aop.aspectj.SingletonAspectInstanceFactory;
import com.chy.summer.framework.core.ordered.OrderUtils;
import com.chy.summer.framework.core.ordered.Ordered;

import java.io.Serializable;

/**
 * 单例的元数据感知切面实例工厂
 */
public class SingletonMetadataAwareAspectInstanceFactory extends SingletonAspectInstanceFactory
		implements MetadataAwareAspectInstanceFactory, Serializable {

	private final AspectMetadata metadata;


	/**
	 * 为给定切面创建一个新的SingletonMetadataAwareAspectInstanceFactory
	 * @param aspectInstance 单例的切面实例
	 * @param aspectName 切面名称
	 */
	public SingletonMetadataAwareAspectInstanceFactory(Object aspectInstance, String aspectName) {
		super(aspectInstance);
		this.metadata = new AspectMetadata(aspectInstance.getClass(), aspectName);
	}

	/**
	 * 获取这个工厂的aspect的AspectJ AspectMetadata。
	 */
	@Override
	public final AspectMetadata getAspectMetadata() {
		return this.metadata;
	}

	/**
	 * 获取此工厂的最佳互斥对象
	 */
	@Override
	public Object getAspectCreationMutex() {
		return this;
	}

	/**
	 * 当没有指定顺序的时候，调用此方法设置后备顺序
	 */
	@Override
	protected int getOrderForAspectClass(Class<?> aspectClass) {
		return OrderUtils.getOrder(aspectClass, Ordered.LOWEST_PRECEDENCE);
	}

}