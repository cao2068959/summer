package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.*;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.context.EnvironmentAware;
import com.chy.summer.framework.core.evn.Environment;
import com.chy.summer.framework.core.io.ResourceLoader;
import com.chy.summer.framework.exception.BeanInstantiationException;
import com.chy.summer.framework.util.Assert;

import java.lang.reflect.Constructor;

/**
 * 用于处理解析器策略的通用委托代码，例如 TypeFilter，ImportSelector，ImportBeanDefinitionRegistrar
 */
public abstract class ParserStrategyUtils {

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
            if (parserStrategyBean instanceof EnvironmentAware) {
                ((EnvironmentAware) parserStrategyBean).setEnvironment(environment);
            }
//			if (parserStrategyBean instanceof ResourceLoaderAware) {
//				((ResourceLoaderAware) parserStrategyBean).setResourceLoader(resourceLoader);
//			}
        }
    }

    public static <T> T instantiateClass(Class<?> clazz, Class<T> assignableTo, Environment environment,
                                         ResourceLoader resourceLoader, BeanDefinitionRegistry registry) {

        Assert.notNull(clazz, "Class must not be null");
        Assert.isAssignable(assignableTo, clazz);
        if (clazz.isInterface()) {
            throw new BeanInstantiationException("[" + clazz + "]+Specified class is an interface");
        }
        ClassLoader classLoader = (registry instanceof ConfigurableBeanFactory ?
                ((ConfigurableBeanFactory) registry).getBeanClassLoader() : resourceLoader.getClassLoader());
        T instance = (T) createInstance(clazz, environment, resourceLoader, registry, classLoader);
        ParserStrategyUtils.invokeAwareMethods(instance, environment, resourceLoader, registry);
        return instance;
    }


    private static Object createInstance(Class<?> clazz, Environment environment,
                                         ResourceLoader resourceLoader, BeanDefinitionRegistry registry,
                                         ClassLoader classLoader) {

        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length == 1 && constructors[0].getParameterCount() > 0) {
            try {
                Constructor<?> constructor = constructors[0];
                Object[] args = resolveArgs(constructor.getParameterTypes(),
                        environment, resourceLoader, registry, classLoader);
                return BeanUtils.instantiateClass(constructor, args);
            } catch (Exception ex) {
                throw new BeanInstantiationException("[" + clazz + "]No suitable constructor found", ex);
            }
        }
        return BeanUtils.instantiateClass(clazz);
    }

	private static Object[] resolveArgs(Class<?>[] parameterTypes,
										Environment environment, ResourceLoader resourceLoader,
										BeanDefinitionRegistry registry, ClassLoader classLoader) {

		Object[] parameters = new Object[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			parameters[i] = resolveParameter(parameterTypes[i], environment,
					resourceLoader, registry, classLoader);
		}
		return parameters;
	}

	private static Object resolveParameter(Class<?> parameterType,
										   Environment environment, ResourceLoader resourceLoader,
										   BeanDefinitionRegistry registry, ClassLoader classLoader) {
		if (parameterType == Environment.class) {
			return environment;
		}
		if (parameterType == ResourceLoader.class) {
			return resourceLoader;
		}
		if (parameterType == BeanFactory.class) {
			return (registry instanceof BeanFactory ? registry : null);
		}
		if (parameterType == ClassLoader.class) {
			return classLoader;
		}
		throw new IllegalStateException("Illegal method parameter type: " + parameterType.getName());
	}



}