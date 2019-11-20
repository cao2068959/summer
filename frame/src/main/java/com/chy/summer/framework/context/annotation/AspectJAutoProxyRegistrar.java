package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.aop.config.AopConfigUtils;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.context.annotation.utils.AnnotationConfigUtils;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.type.AnnotationMetadata;

/**
 * 解析EnableAspectJAutoProxy注释，注册AnnotationAwareAspectJAutoProxyCreator
 */
public class AspectJAutoProxyRegistrar implements ImportBeanDefinitionRegistrar {

	/**
	 * 根据导入的@Configuration类上的proxyTargetClass()属性的值，注册，升级和配置AspectJ自动代理创建器
	 */
	@Override
	public void registerBeanDefinitions(
			AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		//注册AnnotationAwareAspectJAutoProxyCreator
		AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry);
		//获取@EnableAspectJAutoProxy注解的属性信息
		AnnotationAttributes enableAspectJAutoProxy = importingClassMetadata.getAnnotationAttributes(EnableAspectJAutoProxy.class);
		if (enableAspectJAutoProxy != null) {
			if (enableAspectJAutoProxy.getRequiredAttribute("proxyTargetClass",Boolean.class)) {
				AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
			}
			if (enableAspectJAutoProxy.getRequiredAttribute("exposeProxy",Boolean.class)) {
				AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
			}
		}
	}

}