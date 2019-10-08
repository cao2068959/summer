package com.chy.summer.framework.aop.framework.autoProxy;

import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.util.Assert;
import com.sun.istack.internal.Nullable;

/**
 * 用于自动代理识别组件的工具类
 */
public abstract class AutoProxyUtils {

	/**
	 * Bean definition attribute that may indicate whether a given bean is supposed
	 * to be proxied with its target class (in case of it getting proxied in the first
	 * place). The value is {@code Boolean.TRUE} or {@code Boolean.FALSE}.
	 * <p>Proxy factories can set this attribute if they built a target class proxy
	 * for a specific bean, and want to enforce that bean can always be cast
	 * to its target class (even if AOP advices get applied through auto-proxying).
	 * @see #shouldProxyTargetClass
	 */
	public static final String PRESERVE_TARGET_CLASS_ATTRIBUTE =
//			Conventions.getQualifiedAttributeName(AutoProxyUtils.class, "preserveTargetClass");
			//TODO GYX 零时使用  写到这里
			getQualifiedAttributeName(AutoProxyUtils.class, "preserveTargetClass");

	/**
	 * Bean definition attribute that indicates the original target class of an
	 * auto-proxied bean, e.g. to be used for the introspection of annotations
	 * on the target class behind an interface-based proxy.
	 * @since 4.2.3
	 * @see #determineTargetClass
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
	 * Determine whether the given bean should be proxied with its target
	 * class rather than its interfaces. Checks the
	 * {@link #PRESERVE_TARGET_CLASS_ATTRIBUTE "preserveTargetClass" attribute}
	 * of the corresponding bean definition.
	 * @param beanFactory the containing ConfigurableListableBeanFactory
	 * @param beanName the name of the bean
	 * @return whether the given bean should be proxied with its target class
	 */
	public static boolean shouldProxyTargetClass(ConfigurableListableBeanFactory beanFactory, @Nullable String beanName) {
//		if (beanName != null && beanFactory.containsBeanDefinition(beanName)) {
//			BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
//			return Boolean.TRUE.equals(bd.getAttribute(PRESERVE_TARGET_CLASS_ATTRIBUTE));
//		}
		return false;
	}

	/**
	 * Determine the original target class for the specified bean, if possible,
	 * otherwise falling back to a regular {@code getType} lookup.
	 * @param beanFactory the containing ConfigurableListableBeanFactory
	 * @param beanName the name of the bean
	 * @return the original target class as stored in the bean definition, if any
	 * @since 4.2.3
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
	 * Expose the given target class for the specified bean, if possible.
	 * @param beanFactory the containing ConfigurableListableBeanFactory
	 * @param beanName the name of the bean
	 * @param targetClass the corresponding target class
	 * @since 4.2.3
	 */
	static void exposeTargetClass(ConfigurableListableBeanFactory beanFactory, @Nullable String beanName,
								  Class<?> targetClass) {

//		if (beanName != null && beanFactory.containsBeanDefinition(beanName)) {
//			beanFactory.getMergedBeanDefinition(beanName).setAttribute(ORIGINAL_TARGET_CLASS_ATTRIBUTE, targetClass);
//		}
	}

}