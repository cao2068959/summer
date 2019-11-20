package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.core.type.AnnotationMetadata;

/**
 * 由在处理@Configuration类时注册其他bean定义的类型所实现的接口
 */
public interface ImportBeanDefinitionRegistrar {

	/**
	 * 根据导入的@Configuration类的给定注释元数据，根据需要注册Bean定义
	 * @param importingClassMetadata 导入类的注释元数据
	 * @param registry 当前的bean定义注册表
	 */
	public void registerBeanDefinitions(
			AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry);

}