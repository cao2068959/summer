package com.chy.summer.framework.aop.aspectj.annotation;

import com.chy.summer.framework.aop.aspectj.SimpleAspectInstanceFactory;
import com.chy.summer.framework.core.ordered.OrderUtils;
import com.chy.summer.framework.core.ordered.Ordered;

public class SimpleMetadataAwareAspectInstanceFactory extends SimpleAspectInstanceFactory
		implements MetadataAwareAspectInstanceFactory {

	private final AspectMetadata metadata;


	/**
	 * 为给定的方面类创建一个新的SimpleMetadataAwareAspectInstanceFactory
	 * @param aspectClass 切面类型
	 * @param aspectName 切面名称
	 */
	public SimpleMetadataAwareAspectInstanceFactory(Class<?> aspectClass, String aspectName) {
		super(aspectClass);
		this.metadata = new AspectMetadata(aspectClass, aspectName);
	}

	/**
	 * 获取这个工厂的aspect的AspectJ AspectMetadata。
	 * @return aspect的元数据
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