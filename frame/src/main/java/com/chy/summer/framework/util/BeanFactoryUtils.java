/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chy.summer.framework.util;

import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.HierarchicalBeanFactory;
import com.chy.summer.framework.beans.config.ListableBeanFactory;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BeanFactoryUtils {

	/**
	 * Separator for generated bean names. If a class name or parent name is not
	 * unique, "#1", "#2" etc will be appended, until the name becomes unique.
	 */
	public static final String GENERATED_BEAN_NAME_SEPARATOR = "#";


	/**
	 * 转换 beanName 的名字,把前面的 & 全给删了
	 * @param name
	 * @return
	 */
	public static String transformedBeanName(String name) {
		Assert.notNull(name, "name 不能为空");
		String beanName = name;
		while (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
			beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
		}
		return beanName;
	}


	public static boolean isFactoryDereference(String name) {
		return (name != null && name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX));
	}


//	/**
//	 * Return the actual bean name, stripping out the factory dereference
//	 * prefix (if any, also stripping repeated factory prefixes if found).
//	 * @param name the name of the bean
//	 * @return the transformed name
//	 * @see BeanFactory#FACTORY_BEAN_PREFIX
//	 */
//	public static String transformedBeanName(String name) {
//		Assert.notNull(name, "'name' must not be null");
//		String beanName = name;
//		while (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
//			beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
//		}
//		return beanName;
//	}

	/**
	 * 判断给定名称是否是默认命名策略生成的Bean名称。（包含“＃...”部分）
	 * @param name bean的名称
	 */
	public static boolean isGeneratedBeanName(@Nullable String name) {
		return (name != null && name.contains(GENERATED_BEAN_NAME_SEPARATOR));
	}



//	/**
//	 * Extract the "raw" bean name from the given (potentially generated) bean name,
//	 * excluding any "#..." suffixes which might have been added for uniqueness.
//	 * @param name the potentially generated bean name
//	 * @return the raw bean name
//	 * @see #GENERATED_BEAN_NAME_SEPARATOR
//	 */
//	public static String originalBeanName(String name) {
//		Assert.notNull(name, "'name' must not be null");
//		int separatorIndex = name.indexOf(GENERATED_BEAN_NAME_SEPARATOR);
//		return (separatorIndex != -1 ? name.substring(0, separatorIndex) : name);
//	}
//
//
//	/**
//	 * Count all beans in any hierarchy in which this factory participates.
//	 * Includes counts of ancestor bean factories.
//	 * <p>Beans that are "overridden" (specified in a descendant factory
//	 * with the same name) are only counted once.
//	 * @param lbf the bean factory
//	 * @return count of beans including those defined in ancestor factories
//	 */
//	public static int countBeansIncludingAncestors(ListableBeanFactory lbf) {
//		return beanNamesIncludingAncestors(lbf).length;
//	}
//
//	/**
//	 * Return all bean names in the factory, including ancestor factories.
//	 * @param lbf the bean factory
//	 * @return the array of matching bean names, or an empty array if none
//	 * @see #beanNamesForTypeIncludingAncestors
//	 */
//	public static String[] beanNamesIncludingAncestors(ListableBeanFactory lbf) {
//		return beanNamesForTypeIncludingAncestors(lbf, Object.class);
//	}
//
//	/**
//	 * Get all bean names for the given type, including those defined in ancestor
//	 * factories. Will return unique names in case of overridden bean definitions.
//	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
//	 * will get initialized. If the object created by the FactoryBean doesn't match,
//	 * the raw FactoryBean itself will be matched against the type.
//	 * <p>This version of {@code beanNamesForTypeIncludingAncestors} automatically
//	 * includes prototypes and FactoryBeans.
//	 * @param lbf the bean factory
//	 * @param type the type that beans must match (as a {@code ResolvableType})
//	 * @return the array of matching bean names, or an empty array if none
//	 * @since 4.2
//	 */
//	public static String[] beanNamesForTypeIncludingAncestors(ListableBeanFactory lbf, ResolvableType type) {
//		Assert.notNull(lbf, "ListableBeanFactory must not be null");
//		String[] result = lbf.getBeanNamesForType(type);
//		if (lbf instanceof HierarchicalBeanFactory) {
//			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
//			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
//				String[] parentResult = beanNamesForTypeIncludingAncestors(
//						(ListableBeanFactory) hbf.getParentBeanFactory(), type);
//				List<String> resultList = new ArrayList<>();
//				resultList.addAll(Arrays.asList(result));
//				for (String beanName : parentResult) {
//					if (!resultList.contains(beanName) && !hbf.containsLocalBean(beanName)) {
//						resultList.add(beanName);
//					}
//				}
//				result = StringUtils.toStringArray(resultList);
//			}
//		}
//		return result;
//	}

	/**
	 * Get all bean names for the given type, including those defined in ancestor
	 * factories. Will return unique names in case of overridden bean definitions.
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * <p>This version of {@code beanNamesForTypeIncludingAncestors} automatically
	 * includes prototypes and FactoryBeans.
	 * @param lbf the bean factory
	 * @param type the type that beans must match (as a {@code Class})
	 * @return the array of matching bean names, or an empty array if none
	 */
	public static String[] beanNamesForTypeIncludingAncestors(ListableBeanFactory lbf, Class<?> type) {
		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		String[] result = lbf.getBeanNamesForType(type,true,true);
		if (lbf instanceof HierarchicalBeanFactory) {
			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
				String[] parentResult = beanNamesForTypeIncludingAncestors(
						(ListableBeanFactory) hbf.getParentBeanFactory(), type);
				List<String> resultList = new ArrayList<>();
				resultList.addAll(Arrays.asList(result));
				for (String beanName : parentResult) {
					if (!resultList.contains(beanName) && !hbf.containsLocalBean(beanName)) {
						resultList.add(beanName);
					}
				}
				result = StringUtils.toStringArray(resultList);
			}
		}
		return result;
	}

	/**
	 * 获取给定类型的所有bean名称，包括所有父类工厂中定义的名称，如果bean定义被覆盖，将返回唯一的名称。
	 * 如果“ allowEagerInit”标志为true，是否考虑由factorybean创建的对象，这意味着将初始化FactoryBeans。
	 * 如果由FactoryBean创建的对象不匹配，则原始FactoryBean本身将与该类型匹配。
	 * 如果“ allowEagerInit”为false，则仅检查原始FactoryBean（不需要初始化每个FactoryBean）。
	 * @param lbf bean工厂
	 * @param includeNonSingletons 是否包含非单例对象
	 * @param allowEagerInit 是否为类型检查初始化由factorybean(或由具有“factory-bean”引用的工厂方法)创建的延迟初始化单例对象和对象。
	 *                       注意，必须立即初始化factorybean以确定它们的类型:因此要注意，为这个标志传递“true”将初始化factorybean和“factory-bean”引用
	 * @param type bean匹配的类型
	 */
	public static String[] beanNamesForTypeIncludingAncestors(
			ListableBeanFactory lbf, Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {

		Assert.notNull(lbf, "ListableBeanFactory不可为空");
		String[] result = lbf.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		//判断这个类是否可以访问父容器
		if (lbf instanceof HierarchicalBeanFactory) {
			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
			//获取父类工厂
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
				//逐级往上层检查
				String[] parentResult = beanNamesForTypeIncludingAncestors(
						(ListableBeanFactory) hbf.getParentBeanFactory(), type, includeNonSingletons, allowEagerInit);
				List<String> resultList = new ArrayList<>();
				//将所有的获取到的bean保存下来
				resultList.addAll(Arrays.asList(result));
				//查询父类的返回值，进行去重
				for (String beanName : parentResult) {
					if (!resultList.contains(beanName) && !hbf.containsLocalBean(beanName)) {
						resultList.add(beanName);
					}
				}
				result = StringUtils.toStringArray(resultList);
			}
		}
		return result;
	}
//
//	/**
//	 * Return all beans of the given type or subtypes, also picking up beans defined in
//	 * ancestor bean factories if the current bean factory is a HierarchicalBeanFactory.
//	 * The returned Map will only contain beans of this type.
//	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
//	 * will get initialized. If the object created by the FactoryBean doesn't match,
//	 * the raw FactoryBean itself will be matched against the type.
//	 * <p><b>Note: Beans of the same name will take precedence at the 'lowest' factory level,
//	 * i.e. such beans will be returned from the lowest factory that they are being found in,
//	 * hiding corresponding beans in ancestor factories.</b> This feature allows for
//	 * 'replacing' beans by explicitly choosing the same bean name in a child factory;
//	 * the bean in the ancestor factory won't be visible then, not even for by-type lookups.
//	 * @param lbf the bean factory
//	 * @param type type of bean to match
//	 * @return the Map of matching bean instances, or an empty Map if none
//	 * @throws BeansException if a bean could not be created
//	 */
//	public static <T> Map<String, T> beansOfTypeIncludingAncestors(ListableBeanFactory lbf, Class<T> type)
//			throws BeansException {
//
//		Assert.notNull(lbf, "ListableBeanFactory must not be null");
//		Map<String, T> result = new LinkedHashMap<>(4);
//		result.putAll(lbf.getBeansOfType(type));
//		if (lbf instanceof HierarchicalBeanFactory) {
//			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
//			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
//				Map<String, T> parentResult = beansOfTypeIncludingAncestors(
//						(ListableBeanFactory) hbf.getParentBeanFactory(), type);
//				parentResult.forEach((beanName, beanType) -> {
//					if (!result.containsKey(beanName) && !hbf.containsLocalBean(beanName)) {
//						result.put(beanName, beanType);
//					}
//				});
//			}
//		}
//		return result;
//	}
//
//	/**
//	 * Return all beans of the given type or subtypes, also picking up beans defined in
//	 * ancestor bean factories if the current bean factory is a HierarchicalBeanFactory.
//	 * The returned Map will only contain beans of this type.
//	 * <p>Does consider objects created by FactoryBeans if the "allowEagerInit" flag is set,
//	 * which means that FactoryBeans will get initialized. If the object created by the
//	 * FactoryBean doesn't match, the raw FactoryBean itself will be matched against the
//	 * type. If "allowEagerInit" is not set, only raw FactoryBeans will be checked
//	 * (which doesn't require initialization of each FactoryBean).
//	 * <p><b>Note: Beans of the same name will take precedence at the 'lowest' factory level,
//	 * i.e. such beans will be returned from the lowest factory that they are being found in,
//	 * hiding corresponding beans in ancestor factories.</b> This feature allows for
//	 * 'replacing' beans by explicitly choosing the same bean name in a child factory;
//	 * the bean in the ancestor factory won't be visible then, not even for by-type lookups.
//	 * @param lbf the bean factory
//	 * @param type type of bean to match
//	 * @param includeNonSingletons whether to include prototype or scoped beans too
//	 * or just singletons (also applies to FactoryBeans)
//	 * @param allowEagerInit whether to initialize <i>lazy-init singletons</i> and
//	 * <i>objects created by FactoryBeans</i> (or by factory methods with a
//	 * "factory-bean" reference) for the type check. Note that FactoryBeans need to be
//	 * eagerly initialized to determine their type: So be aware that passing in "true"
//	 * for this flag will initialize FactoryBeans and "factory-bean" references.
//	 * @return the Map of matching bean instances, or an empty Map if none
//	 * @throws BeansException if a bean could not be created
//	 */
//	public static <T> Map<String, T> beansOfTypeIncludingAncestors(
//			ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
//			throws BeansException {
//
//		Assert.notNull(lbf, "ListableBeanFactory must not be null");
//		Map<String, T> result = new LinkedHashMap<>(4);
//		result.putAll(lbf.getBeansOfType(type, includeNonSingletons, allowEagerInit));
//		if (lbf instanceof HierarchicalBeanFactory) {
//			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
//			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
//				Map<String, T> parentResult = beansOfTypeIncludingAncestors(
//						(ListableBeanFactory) hbf.getParentBeanFactory(), type, includeNonSingletons, allowEagerInit);
//				parentResult.forEach((beanName, beanType) -> {
//					if (!result.containsKey(beanName) && !hbf.containsLocalBean(beanName)) {
//						result.put(beanName, beanType);
//					}
//				});
//			}
//		}
//		return result;
//	}
//
//
//	/**
//	 * Return a single bean of the given type or subtypes, also picking up beans
//	 * defined in ancestor bean factories if the current bean factory is a
//	 * HierarchicalBeanFactory. Useful convenience method when we expect a
//	 * single bean and don't care about the bean name.
//	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
//	 * will get initialized. If the object created by the FactoryBean doesn't match,
//	 * the raw FactoryBean itself will be matched against the type.
//	 * <p>This version of {@code beanOfTypeIncludingAncestors} automatically includes
//	 * prototypes and FactoryBeans.
//	 * <p><b>Note: Beans of the same name will take precedence at the 'lowest' factory level,
//	 * i.e. such beans will be returned from the lowest factory that they are being found in,
//	 * hiding corresponding beans in ancestor factories.</b> This feature allows for
//	 * 'replacing' beans by explicitly choosing the same bean name in a child factory;
//	 * the bean in the ancestor factory won't be visible then, not even for by-type lookups.
//	 * @param lbf the bean factory
//	 * @param type type of bean to match
//	 * @return the matching bean instance
//	 * @throws NoSuchBeanDefinitionException if no bean of the given type was found
//	 * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
//	 * @throws BeansException if the bean could not be created
//	 */
//	public static <T> T beanOfTypeIncludingAncestors(ListableBeanFactory lbf, Class<T> type)
//			throws BeansException {
//
//		Map<String, T> beansOfType = beansOfTypeIncludingAncestors(lbf, type);
//		return uniqueBean(type, beansOfType);
//	}
//
//	/**
//	 * Get all bean names whose {@code Class} has the supplied {@link Annotation}
//	 * type, including those defined in ancestor factories, without creating any bean
//	 * instances yet. Will return unique names in case of overridden bean definitions.
//	 * @param lbf the bean factory
//	 * @param annotationType the type of annotation to look for
//	 * @return the array of matching bean names, or an empty array if none
//	 * @since 5.0
//	 */
//	public static String[] beanNamesForAnnotationIncludingAncestors(
//			ListableBeanFactory lbf, Class<? extends Annotation> annotationType) {
//		Assert.notNull(lbf, "ListableBeanFactory must not be null");
//		String[] result = lbf.getBeanNamesForAnnotation(annotationType);
//		if (lbf instanceof HierarchicalBeanFactory) {
//			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
//			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
//				String[] parentResult = beanNamesForAnnotationIncludingAncestors(
//						(ListableBeanFactory) hbf.getParentBeanFactory(), annotationType);
//				List<String> resultList = new ArrayList<>();
//				resultList.addAll(Arrays.asList(result));
//				for (String beanName : parentResult) {
//					if (!resultList.contains(beanName) && !hbf.containsLocalBean(beanName)) {
//						resultList.add(beanName);
//					}
//				}
//				result = StringUtils.toStringArray(resultList);
//			}
//		}
//		return result;
//	}
//
//	/**
//	 * Return a single bean of the given type or subtypes, also picking up beans
//	 * defined in ancestor bean factories if the current bean factory is a
//	 * HierarchicalBeanFactory. Useful convenience method when we expect a
//	 * single bean and don't care about the bean name.
//	 * <p>Does consider objects created by FactoryBeans if the "allowEagerInit" flag is set,
//	 * which means that FactoryBeans will get initialized. If the object created by the
//	 * FactoryBean doesn't match, the raw FactoryBean itself will be matched against the
//	 * type. If "allowEagerInit" is not set, only raw FactoryBeans will be checked
//	 * (which doesn't require initialization of each FactoryBean).
//	 * <p><b>Note: Beans of the same name will take precedence at the 'lowest' factory level,
//	 * i.e. such beans will be returned from the lowest factory that they are being found in,
//	 * hiding corresponding beans in ancestor factories.</b> This feature allows for
//	 * 'replacing' beans by explicitly choosing the same bean name in a child factory;
//	 * the bean in the ancestor factory won't be visible then, not even for by-type lookups.
//	 * @param lbf the bean factory
//	 * @param type type of bean to match
//	 * @param includeNonSingletons whether to include prototype or scoped beans too
//	 * or just singletons (also applies to FactoryBeans)
//	 * @param allowEagerInit whether to initialize <i>lazy-init singletons</i> and
//	 * <i>objects created by FactoryBeans</i> (or by factory methods with a
//	 * "factory-bean" reference) for the type check. Note that FactoryBeans need to be
//	 * eagerly initialized to determine their type: So be aware that passing in "true"
//	 * for this flag will initialize FactoryBeans and "factory-bean" references.
//	 * @return the matching bean instance
//	 * @throws NoSuchBeanDefinitionException if no bean of the given type was found
//	 * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
//	 * @throws BeansException if the bean could not be created
//	 */
//	public static <T> T beanOfTypeIncludingAncestors(
//			ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
//			throws BeansException {
//
//		Map<String, T> beansOfType = beansOfTypeIncludingAncestors(lbf, type, includeNonSingletons, allowEagerInit);
//		return uniqueBean(type, beansOfType);
//	}
//
//	/**
//	 * Return a single bean of the given type or subtypes, not looking in ancestor
//	 * factories. Useful convenience method when we expect a single bean and
//	 * don't care about the bean name.
//	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
//	 * will get initialized. If the object created by the FactoryBean doesn't match,
//	 * the raw FactoryBean itself will be matched against the type.
//	 * <p>This version of {@code beanOfType} automatically includes
//	 * prototypes and FactoryBeans.
//	 * @param lbf the bean factory
//	 * @param type type of bean to match
//	 * @return the matching bean instance
//	 * @throws NoSuchBeanDefinitionException if no bean of the given type was found
//	 * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
//	 * @throws BeansException if the bean could not be created
//	 */
//	public static <T> T beanOfType(ListableBeanFactory lbf, Class<T> type) throws BeansException {
//		Assert.notNull(lbf, "ListableBeanFactory must not be null");
//		Map<String, T> beansOfType = lbf.getBeansOfType(type);
//		return uniqueBean(type, beansOfType);
//	}
//
//	/**
//	 * Return a single bean of the given type or subtypes, not looking in ancestor
//	 * factories. Useful convenience method when we expect a single bean and
//	 * don't care about the bean name.
//	 * <p>Does consider objects created by FactoryBeans if the "allowEagerInit"
//	 * flag is set, which means that FactoryBeans will get initialized. If the
//	 * object created by the FactoryBean doesn't match, the raw FactoryBean itself
//	 * will be matched against the type. If "allowEagerInit" is not set,
//	 * only raw FactoryBeans will be checked (which doesn't require initialization
//	 * of each FactoryBean).
//	 * @param lbf the bean factory
//	 * @param type type of bean to match
//	 * @param includeNonSingletons whether to include prototype or scoped beans too
//	 * or just singletons (also applies to FactoryBeans)
//	 * @param allowEagerInit whether to initialize <i>lazy-init singletons</i> and
//	 * <i>objects created by FactoryBeans</i> (or by factory methods with a
//	 * "factory-bean" reference) for the type check. Note that FactoryBeans need to be
//	 * eagerly initialized to determine their type: So be aware that passing in "true"
//	 * for this flag will initialize FactoryBeans and "factory-bean" references.
//	 * @return the matching bean instance
//	 * @throws NoSuchBeanDefinitionException if no bean of the given type was found
//	 * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
//	 * @throws BeansException if the bean could not be created
//	 */
//	public static <T> T beanOfType(
//			ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
//			throws BeansException {
//
//		Assert.notNull(lbf, "ListableBeanFactory must not be null");
//		Map<String, T> beansOfType = lbf.getBeansOfType(type, includeNonSingletons, allowEagerInit);
//		return uniqueBean(type, beansOfType);
//	}
//
//	/**
//	 * Extract a unique bean for the given type from the given Map of matching beans.
//	 * @param type type of bean to match
//	 * @param matchingBeans all matching beans found
//	 * @return the unique bean instance
//	 * @throws NoSuchBeanDefinitionException if no bean of the given type was found
//	 * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
//	 */
//	private static <T> T uniqueBean(Class<T> type, Map<String, T> matchingBeans) {
//		int nrFound = matchingBeans.size();
//		if (nrFound == 1) {
//			return matchingBeans.values().iterator().next();
//		}
//		else if (nrFound > 1) {
//			throw new NoUniqueBeanDefinitionException(type, matchingBeans.keySet());
//		}
//		else {
//			throw new NoSuchBeanDefinitionException(type);
//		}
//	}

}
