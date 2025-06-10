package com.chy.summer.framework.aop.aspectj.annotation;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.beans.config.ListableBeanFactory;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.BeanFactoryUtils;
import javax.annotation.Nullable;
import org.aspectj.lang.reflect.PerClauseKind;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AOP内部工具类，该工具类用来从bean容器，也就是BeanFactory中获取所有使用了@AspectJ注解的bean，最终用于自动代理机制
 * BeanFactoryAspectJAdvisorsBuilder最核心的逻辑在其方法buildAspectJAdvisors中，
 * 该方法查找容器中所有@AspectJ注解的bean,然后将其中每个advice方法包装成一个Advisor。最终结果以一个List<Advisor>的形式返回给调用者。
 */
public class BeanFactoryAspectJAdvisorsBuilder {

    /**
     * 用来扫描的ListableBeanFactory
     */
    private final ListableBeanFactory beanFactory;

    /**
     * 用来构建Advisor的AspectJAdvisorFactory
     */
	private final AspectJAdvisorFactory advisorFactory;

	@Nullable
	private volatile List<String> aspectBeanNames;

    /**
     * advisor缓存
     */
	private final Map<String, List<Advisor>> advisorsCache = new ConcurrentHashMap<>();

    /**
     * advisor工厂缓存
     */
	private final Map<String, MetadataAwareAspectInstanceFactory> aspectFactoryCache = new ConcurrentHashMap<>();


	/**
	 * 为给定的BeanFactory创建一个新的BeanFactoryAspectJAdvisorsBuilder
	 * @param beanFactory the ListableBeanFactory to scan
	 */
	public BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory) {
		this(beanFactory, new ReflectiveAspectJAdvisorFactory(beanFactory));
	}

	/**
	 * 为给定的BeanFactory创建一个新的BeanFactoryAspectJAdvisorsBuilder
	 * @param beanFactory 用来扫描的ListableBeanFactory
	 * @param advisorFactory 用来构建Advisor的AspectJAdvisorFactory
	 */
	public BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {
		Assert.notNull(beanFactory, "ListableBeanFactory不可为空");
		Assert.notNull(advisorFactory, "AspectJAdvisorFactory不可为空");
		this.beanFactory = beanFactory;
		this.advisorFactory = advisorFactory;
	}


	/**
	 * 在当前的bean工厂中查找带有AspectJ注释的aspect bean，然后返回它们的Advisor列表
	 * 为每个AspectJ advice方法创建一个Advisor
	 */
	public List<Advisor> buildAspectJAdvisors() {
		List<String> aspectNames = this.aspectBeanNames;

		if (aspectNames == null) {
			synchronized (this) {
				aspectNames = this.aspectBeanNames;
				if (aspectNames == null) {
					List<Advisor> advisors = new LinkedList<>();
					aspectNames = new LinkedList<>();
					//获取给定类型的所有bean名称，这里就是获取所有bean
					String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
							this.beanFactory, Object.class, true, false);

					for (String beanName : beanNames) {
						if (!isEligibleBean(beanName)) {
							continue;
						}
						//从容器中获取bean的类型
						Class<?> beanType = this.beanFactory.getType(beanName);
						if (beanType == null) {
							continue;
						}
						if (this.advisorFactory.isAspect(beanType)) {
						    //加入切面名称的列表
							aspectNames.add(beanName);
							//创建切面元数据
							AspectMetadata amd = new AspectMetadata(beanType, beanName);
							if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
								MetadataAwareAspectInstanceFactory factory =
										new BeanFactoryAspectInstanceFactory(this.beanFactory, beanName);
								//为aspect实例上的所有带有AspectJ注释方法构建summer AOP advisor
								List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);
								if (this.beanFactory.isSingleton(beanName)) {
								    //单例bean存放到advisor缓存中
									this.advisorsCache.put(beanName, classAdvisors);
								}
								else {
								    //非单例bean ，缓存他的工厂
									this.aspectFactoryCache.put(beanName, factory);
								}
								advisors.addAll(classAdvisors);
							}
							else {
								if (this.beanFactory.isSingleton(beanName)) {
									throw new IllegalArgumentException("'" + beanName +
											"' 是一个单例bean, 但是切面实例化模型不可以是单例");
								}
								MetadataAwareAspectInstanceFactory factory =
										new PrototypeAspectInstanceFactory(this.beanFactory, beanName);
								this.aspectFactoryCache.put(beanName, factory);
								advisors.addAll(this.advisorFactory.getAdvisors(factory));
							}
						}
					}
					this.aspectBeanNames = aspectNames;
					return advisors;
				}
			}
		}

		if (aspectNames.isEmpty()) {
			return Collections.emptyList();
		}
		List<Advisor> advisors = new LinkedList<>();
		//已经创建过一次AspectJAdvisors了，直接从缓存中获取
		for (String aspectName : aspectNames) {
			List<Advisor> cachedAdvisors = this.advisorsCache.get(aspectName);
			if (cachedAdvisors != null) {
			    //获取单例bean的advisor
				advisors.addAll(cachedAdvisors);
			}
			else {
			    //获取原型bean的advisor
				MetadataAwareAspectInstanceFactory factory = this.aspectFactoryCache.get(aspectName);
				advisors.addAll(this.advisorFactory.getAdvisors(factory));
			}
		}
		return advisors;
	}

	/**
	 * Aspect Bean的名称是否合格
	 */
	protected boolean isEligibleBean(String beanName) {
		return true;
	}

}