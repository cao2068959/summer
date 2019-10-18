package com.chy.summer.framework.core.annotation;

import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ObjectUtils;
import com.chy.summer.framework.util.ReflectionUtils;
import com.chy.summer.framework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 为已经合成具有附加功能的注释进行处理(即，包装在动态代理中的注解)
 */
class SynthesizedAnnotationInvocationHandler implements InvocationHandler {

	private final AnnotationAttributeExtractor<?> attributeExtractor;

	private final Map<String, Object> valueCache = new ConcurrentHashMap<>(8);


	/**
	 * 使用提供的AnnotationAttributeExtractor构造一个新的SynthesizedAnnotationInvocationHandler
	 */
	SynthesizedAnnotationInvocationHandler(AnnotationAttributeExtractor<?> attributeExtractor) {
		Assert.notNull(attributeExtractor, "AnnotationAttributeExtractor不可为空");
		this.attributeExtractor = attributeExtractor;
	}

	/**
	 * 执行方法
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		//执行equals方法
		if (ReflectionUtils.isEqualsMethod(method)) {
			return annotationEquals(args[0]);
		}
		//执行hashCode方法
		if (ReflectionUtils.isHashCodeMethod(method)) {
			return annotationHashCode();
		}
		//执行toString方法
		if (ReflectionUtils.isToStringMethod(method)) {
			return annotationToString();
		}
		//执行isAnnotationType方法
		if (AnnotationUtils.isAnnotationTypeMethod(method)) {
			return annotationType();
		}
		if (!AnnotationUtils.isAttributeMethod(method)) {
			throw new AnnotationConfigurationException(String.format(
					"不支持合成注释类型[%s]的方法[%s]", method, annotationType()));
		}
		return getAttributeValue(method);
	}

	private Class<? extends Annotation> annotationType() {
		return this.attributeExtractor.getAnnotationType();
	}

	/**
	 * 获取属性值
	 */
	private Object getAttributeValue(Method attributeMethod) {
		//获取参数名
		String attributeName = attributeMethod.getName();
		//尝试从缓存中获取
		Object value = this.valueCache.get(attributeName);
		if (value == null) {
			//使用注释属性提取器获取指定的参数值
			value = this.attributeExtractor.getAttributeValue(attributeMethod);
			if (value == null) {
				String msg = String.format("执行器%s，属性源[%s]中属性名[%s]返回null",
						this.attributeExtractor.getClass().getName(), attributeName, this.attributeExtractor.getSource());
				throw new IllegalStateException(msg);
			}

			// 在返回嵌套注释之前进行合并
			if (value instanceof Annotation) {
				value = AnnotationUtils.synthesizeAnnotation((Annotation) value, this.attributeExtractor.getAnnotatedElement());
			}
			//注解数组合并
			else if (value instanceof Annotation[]) {
				value = AnnotationUtils.synthesizeAnnotationArray((Annotation[]) value, this.attributeExtractor.getAnnotatedElement());
			}

			this.valueCache.put(attributeName, value);
		}

		//克隆数组，这样用户就不能改变缓存中值的内容
		if (value.getClass().isArray()) {
			value = cloneArray(value);
		}

		return value;
	}

	/**
	 * 克隆提供的数组，确保保留原始组件类型
	 * @param array 需要克隆的数组
	 */
	private Object cloneArray(Object array) {
		if (array instanceof boolean[]) {
			return ((boolean[]) array).clone();
		}
		if (array instanceof byte[]) {
			return ((byte[]) array).clone();
		}
		if (array instanceof char[]) {
			return ((char[]) array).clone();
		}
		if (array instanceof double[]) {
			return ((double[]) array).clone();
		}
		if (array instanceof float[]) {
			return ((float[]) array).clone();
		}
		if (array instanceof int[]) {
			return ((int[]) array).clone();
		}
		if (array instanceof long[]) {
			return ((long[]) array).clone();
		}
		if (array instanceof short[]) {
			return ((short[]) array).clone();
		}

		// else
		return ((Object[]) array).clone();
	}

	/**
	 * 判断注解是否相等
	 */
	private boolean annotationEquals(Object other) {
		if (this == other) {
			return true;
		}
		//没有继承关系
		if (!annotationType().isInstance(other)) {
			return false;
		}

		for (Method attributeMethod : AnnotationUtils.getAttributeMethods(annotationType())) {
			//获取属性值，进行对比
			Object thisValue = getAttributeValue(attributeMethod);
			Object otherValue = ReflectionUtils.invokeMethod(attributeMethod, other);
			if (!ObjectUtils.nullSafeEquals(thisValue, otherValue)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 获取注解的hashCode
	 */
	private int annotationHashCode() {
		int result = 0;

		for (Method attributeMethod : AnnotationUtils.getAttributeMethods(annotationType())) {
			Object value = getAttributeValue(attributeMethod);
			int hashCode;
			if (value.getClass().isArray()) {
				hashCode = hashCodeForArray(value);
			}
			else {
				hashCode = value.hashCode();
			}
			result += (127 * attributeMethod.getName().hashCode()) ^ hashCode;
		}

		return result;
	}

	private int hashCodeForArray(Object array) {
		if (array instanceof boolean[]) {
			return Arrays.hashCode((boolean[]) array);
		}
		if (array instanceof byte[]) {
			return Arrays.hashCode((byte[]) array);
		}
		if (array instanceof char[]) {
			return Arrays.hashCode((char[]) array);
		}
		if (array instanceof double[]) {
			return Arrays.hashCode((double[]) array);
		}
		if (array instanceof float[]) {
			return Arrays.hashCode((float[]) array);
		}
		if (array instanceof int[]) {
			return Arrays.hashCode((int[]) array);
		}
		if (array instanceof long[]) {
			return Arrays.hashCode((long[]) array);
		}
		if (array instanceof short[]) {
			return Arrays.hashCode((short[]) array);
		}

		// else
		return Arrays.hashCode((Object[]) array);
	}

	/**
	 * toString方法
	 */
	private String annotationToString() {
		StringBuilder sb = new StringBuilder("@").append(annotationType().getName()).append("(");

		Iterator<Method> iterator = AnnotationUtils.getAttributeMethods(annotationType()).iterator();
		while (iterator.hasNext()) {
			Method attributeMethod = iterator.next();
			sb.append(attributeMethod.getName());
			sb.append('=');
			sb.append(attributeValueToString(getAttributeValue(attributeMethod)));
			sb.append(iterator.hasNext() ? ", " : "");
		}

		return sb.append(")").toString();
	}

	private String attributeValueToString(Object value) {
		if (value instanceof Object[]) {
			return "[" + StringUtils.arrayToDelimitedString((Object[]) value, ", ") + "]";
		}
		return String.valueOf(value);
	}

}
