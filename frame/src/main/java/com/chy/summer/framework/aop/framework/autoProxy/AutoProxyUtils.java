package com.chy.summer.framework.aop.framework.autoProxy;

import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.util.Assert;
import javax.annotation.Nullable;

/**
 * 用于自动代理识别组件的工具类
 */
public abstract class AutoProxyUtils {

	/**
	 * Bean定义属性，该属性可以指示是否应使用给定的目标类代理给定的Bean
	 */
	public static final String PRESERVE_TARGET_CLASS_ATTRIBUTE =
//			Conventions.getQualifiedAttributeName(AutoProxyUtils.class, "preserveTargetClass");
			//TODO GYX 零时使用
			getQualifiedAttributeName(AutoProxyUtils.class, "preserveTargetClass");

	/**
	 * Bean定义属性，指示自动代理Bean的原始目标类
	 */
	public static final String ORIGINAL_TARGET_CLASS_ATTRIBUTE =
//			Conventions.getQualifiedAttributeName(AutoProxyUtils.class, "originalTargetClass");
			getQualifiedAttributeName(AutoProxyUtils.class, "originalTargetClass");

	//零时的方法
	public static String getQualifiedAttributeName(Class<?> enclosingClass, String attributeName) {
		Assert.notNull(enclosingClass, "'enclosingClass' must not be null");
		Assert.notNull(attributeName, "'attributeName' must not be null");
		return enclosingClass.getName() + "." + attributeName;
	}

	/**
	 * 判断是否应使用给定的bean替代其目标类而不是其接口
	 * @param beanFactory 包含的ConfigurableListableBeanFactory
	 * @param beanName bean的名称
	 */
	public static boolean shouldProxyTargetClass(ConfigurableListableBeanFactory beanFactory, @Nullable String beanName) {
//		if (beanName != null && beanFactory.containsBeanDefinition(beanName)) {
//			BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
//			//TODO GTX 差一个容器 getAttribute 下面也是的
//			return Boolean.TRUE.equals(bd.getAttribute(PRESERVE_TARGET_CLASS_ATTRIBUTE));
//		}
		return false;
	}

	/**
	 * 确定指定bean的原始目标类，如果找不到则返回常规getType
	 *
	 * @param beanFactory 包含的ConfigurableListableBeanFactory
	 * @param beanName bean名称
	 */
	@Nullable
	public static Class<?> determineTargetClass(ConfigurableListableBeanFactory beanFactory, @Nullable String beanName) {
		if (beanName == null) {
			return null;
		}
//		if (beanFactory.containsBeanDefinition(beanName)) {
//			BeanDefinition bd = beanFactory.getMergedBeanDefinition(beanName);
//			Class<?> targetClass = (Class<?>) bd.getAttribute(ORIGINAL_TARGET_CLASS_ATTRIBUTE);
//			if (targetClass != null) {
//				return targetClass;
//			}
//		}
		return beanFactory.getType(beanName);
	}

	/**
	 * 暴露给定目标类的bean。
	 * @param beanFactory 包含的ConfigurableListableBeanFactory
	 * @param beanName bean名称
	 * @param targetClass 对应的目标类别
	 * @since 4.2.3
	 */
	static void exposeTargetClass(ConfigurableListableBeanFactory beanFactory, @Nullable String beanName,
								  Class<?> targetClass) {

//		if (beanName != null && beanFactory.containsBeanDefinition(beanName)) {
//			beanFactory.getMergedBeanDefinition(beanName).setAttribute(ORIGINAL_TARGET_CLASS_ATTRIBUTE, targetClass);
//		}
	}

}