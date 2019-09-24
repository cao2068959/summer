package com.chy.summer.framework.aop.aspectj.annotation;

import com.chy.summer.framework.aop.aspectj.AspectInstanceFactory;
import com.sun.istack.internal.Nullable;

/**
 * 返回与aspectj注释类关联的AspectMetadata的AspectInstanceFactory子接口。
 * 理想情况下，AspectInstanceFactory应该包含这个方法本身，但是通常我们需要分割这个子接口。
 */
public interface MetadataAwareAspectInstanceFactory extends AspectInstanceFactory {

	/**
	 * 获取这个工厂的aspect的AspectJ AspectMetadata。
	 * @return aspect的元数据
	 */
	AspectMetadata getAspectMetadata();

	/**
	 * 获取此工厂的最佳互斥对象
	 */
	@Nullable
	Object getAspectCreationMutex();

}