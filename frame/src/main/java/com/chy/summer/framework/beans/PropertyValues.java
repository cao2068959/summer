package com.chy.summer.framework.beans;

import javax.annotation.Nullable;

/**
 * 代表大量属性的接口，一般放在 beanDefintion 里面，这样就代表了一个类上的所有属性
 */
public interface PropertyValues {

	/**
	 * 返回了所有的 属性对象
	 */
	PropertyValue[] getPropertyValues();

	/**
	 * 通过属性的 名称来获取对应的属性值
	 */
	@Nullable
	PropertyValue getPropertyValue(String propertyName);

	PropertyValues changesSince(PropertyValues old);

	/**
	 * 查看是否包含某一个属性
	 */
	boolean contains(String propertyName);

	/**
	 * 是不是没有任何一个属性
	 */
	boolean isEmpty();

}