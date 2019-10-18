package com.chy.summer.framework.core.annotation;

import com.chy.summer.framework.util.ReflectionUtils;
import com.sun.istack.internal.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 默认注释属性提取器
 */
class DefaultAnnotationAttributeExtractor extends AbstractAliasAwareAnnotationAttributeExtractor<Annotation> {

	DefaultAnnotationAttributeExtractor(Annotation annotation, @Nullable Object annotatedElement) {
		super(annotation.annotationType(), annotatedElement, annotation);
	}

	/**
	 * 从提供的属性方法对应的底层getSource获取原始的、未修改的属性值
	 */
	@Override
	@Nullable
	protected Object getRawAttributeValue(Method attributeMethod) {
		ReflectionUtils.makeAccessible(attributeMethod);
		return ReflectionUtils.invokeMethod(attributeMethod, getSource());
	}

	/**
	 * 从提供的属性名对应的底层getSource获取原始的、未修改的属性值
	 */
	@Override
	@Nullable
	protected Object getRawAttributeValue(String attributeName) {
		Method attributeMethod = ReflectionUtils.findMethod(getAnnotationType(), attributeName);
		return (attributeMethod != null ? getRawAttributeValue(attributeMethod) : null);
	}

}