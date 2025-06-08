package com.chy.summer.framework.core.annotation;


import javax.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * AnnotationAttributeExtractor负责从底层getSource(如注释或映射)获取attributevalue注释属性值
 */
interface AnnotationAttributeExtractor<S> {

	/**
	 * 获取此提取器为其提取属性值的注释的类型（就是提取需要提取的那个注解）
	 */
	Class<? extends Annotation> getAnnotationType();

	/**
	 * 获取由该提取程序支持的注释类型的注释所注释的元素（注解所在的那个元素）
	 */
	@Nullable
	Object getAnnotatedElement();

	/**
	 * 获取注释属性的基础源
	 */
	S getSource();

	/**
	 * 从与提供的属性方法对应的底层getSource源获取属性值
	 * @param attributeMethod 此提取程序支持的注释类型的属性方法
	 */
	@Nullable
	Object getAttributeValue(Method attributeMethod);

}
