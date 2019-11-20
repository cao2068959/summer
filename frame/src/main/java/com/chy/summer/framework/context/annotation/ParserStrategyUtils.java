package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.*;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.core.evn.Environment;
import com.chy.summer.framework.core.io.ResourceLoader;

/**
 * 用于处理解析器策略的通用委托代码，例如 TypeFilter，ImportSelector，ImportBeanDefinitionRegistrar
 */
abstract class ParserStrategyUtils {

	public static void invokeAwareMethods(Object parserStrategyBean, Environment environment,
										  ResourceLoader resourceLoader, BeanDefinitionRegistry registry) {

		if (parserStrategyBean instanceof Aware) {
			if (parserStrategyBean instanceof BeanClassLoaderAware) {
				ClassLoader classLoader = (registry instanceof ConfigurableBeanFactory ?
						((ConfigurableBeanFactory) registry).getBeanClassLoader() : resourceLoader.getClassLoader());
				if (classLoader != null) {
					((BeanClassLoaderAware) parserStrategyBean).setBeanClassLoader(classLoader);
				}
			}
			if (parserStrategyBean instanceof BeanFactoryAware && registry instanceof BeanFactory) {
				((BeanFactoryAware) parserStrategyBean).setBeanFactory((BeanFactory) registry);
			}
//			if (parserStrategyBean instanceof EnvironmentAware) {
//				((EnvironmentAware) parserStrategyBean).setEnvironment(environment);
//			}
//			if (parserStrategyBean instanceof ResourceLoaderAware) {
//				((ResourceLoaderAware) parserStrategyBean).setResourceLoader(resourceLoader);
//			}
		}
	}

}