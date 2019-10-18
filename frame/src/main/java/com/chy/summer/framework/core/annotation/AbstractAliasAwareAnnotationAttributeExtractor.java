package com.chy.summer.framework.core.annotation;

import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ObjectUtils;
import com.sun.istack.internal.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * 注释属性提取器实现的抽象基类，该实现使用@AliasFor注释的注释属性强制执行属性别名
 */
abstract class AbstractAliasAwareAnnotationAttributeExtractor<S> implements AnnotationAttributeExtractor<S> {

	private final Class<? extends Annotation> annotationType;

	@Nullable
	private final Object annotatedElement;

	private final S source;

    /**
     * 属性类型和属性别名映射
     */
	private final Map<String, List<String>> attributeAliasMap;


	/**
	 * 创建一个新的AbstractAliasAwareAnnotationAttributeExtractor
	 * @param annotationType 要合成的注释类型； 不可为null
	 * @param annotatedElement 使用提供的类型的注释进行注释的元素;未知时可能为null
	 * @param source 注释属性的基础来源； 不可为null
	 */
	AbstractAliasAwareAnnotationAttributeExtractor(
			Class<? extends Annotation> annotationType, @Nullable Object annotatedElement, S source) {

		Assert.notNull(annotationType, "annotationType不可为空");
		Assert.notNull(source, "source不可为空");
		this.annotationType = annotationType;
		this.annotatedElement = annotatedElement;
		this.source = source;
		this.attributeAliasMap = AnnotationUtils.getAttributeAliasMap(annotationType);
	}

    /**
     * 获取此提取器为其提取属性值的注释的类型（就是提取需要提取的那个注解）
     */
	@Override
	public final Class<? extends Annotation> getAnnotationType() {
		return this.annotationType;
	}

    /**
     * 获取注释属性的基础源
     */
	@Override
	@Nullable
	public final Object getAnnotatedElement() {
		return this.annotatedElement;
	}

	/**
     * 获取注释属性的基础源
     */
	@Override
	public final S getSource() {
		return this.source;
	}

    /**
     * 从与提供的属性方法对应的底层getSource源获取属性值
     * @param attributeMethod 此提取程序支持的注释类型的属性方法
     */
	@Override
	@Nullable
	public final Object getAttributeValue(Method attributeMethod) {
	    //获取属性名
		String attributeName = attributeMethod.getName();
		//获取属性值
		Object attributeValue = getRawAttributeValue(attributeMethod);

		//获取别名
		List<String> aliasNames = this.attributeAliasMap.get(attributeName);
		if (aliasNames != null) {
		    //获取注解对应属性的默认属性值
			Object defaultValue = AnnotationUtils.getDefaultValue(this.annotationType, attributeName);
			for (String aliasName : aliasNames) {
			    //获取别名的值
				Object aliasValue = getRawAttributeValue(aliasName);

				if (!ObjectUtils.nullSafeEquals(attributeValue, aliasValue) &&
						!ObjectUtils.nullSafeEquals(attributeValue, defaultValue) &&
						!ObjectUtils.nullSafeEquals(aliasValue, defaultValue)) {
					String elementName = (this.annotatedElement != null ? this.annotatedElement.toString() : "未知元素");
					throw new AnnotationConfigurationException(String.format(
							"在%s上声明并由[%s]合成的注释[%s]中，属性'%s'及其别名'%s'的值分别为[%s]和[%s]，但只允许有一个",
							this.annotationType.getName(), elementName, this.source, attributeName, aliasName,
							ObjectUtils.nullSafeToString(attributeValue), ObjectUtils.nullSafeToString(aliasValue)));
				}

				//如果用户没有使用显式值声明注释，则使用别名的值
				if (ObjectUtils.nullSafeEquals(attributeValue, defaultValue)) {
					attributeValue = aliasValue;
				}
			}
		}

		return attributeValue;
	}


	/**
	 * 从提供的属性方法对应的底层getSource获取原始的、未修改的属性值
	 */
	@Nullable
	protected abstract Object getRawAttributeValue(Method attributeMethod);

	/**
	 * 从提供的属性名对应的底层getSource获取原始的、未修改的属性值
	 */
	@Nullable
	protected abstract Object getRawAttributeValue(String attributeName);

}