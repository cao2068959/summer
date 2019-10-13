//package com.chy.summer.framework.aop.aspectj.annotation;
//
//import com.chy.summer.framework.aop.Advisor;
//import com.chy.summer.framework.beans.config.ListableBeanFactory;
//import com.chy.summer.framework.util.Assert;
//import com.chy.summer.framework.util.BeanFactoryUtils;
//import com.sun.istack.internal.Nullable;
//import org.aspectj.lang.reflect.PerClauseKind;
//
//import java.util.Collections;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class BeanFactoryAspectJAdvisorsBuilder {
//
//	private final ListableBeanFactory beanFactory;
//
//	private final AspectJAdvisorFactory advisorFactory;
//
//	@Nullable
//	private volatile List<String> aspectBeanNames;
//
//	private final Map<String, List<Advisor>> advisorsCache = new ConcurrentHashMap<>();
//
//	private final Map<String, MetadataAwareAspectInstanceFactory> aspectFactoryCache = new ConcurrentHashMap<>();
//
//
//	/**
//	 * Create a new BeanFactoryAspectJAdvisorsBuilder for the given BeanFactory.
//	 * @param beanFactory the ListableBeanFactory to scan
//	 */
//	public BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory) {
//		this(beanFactory, new ReflectiveAspectJAdvisorFactory(beanFactory));
//	}
//
//	/**
//	 * Create a new BeanFactoryAspectJAdvisorsBuilder for the given BeanFactory.
//	 * @param beanFactory the ListableBeanFactory to scan
//	 * @param advisorFactory the AspectJAdvisorFactory to build each Advisor with
//	 */
//	public BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {
//		Assert.notNull(beanFactory, "ListableBeanFactory must not be null");
//		Assert.notNull(advisorFactory, "AspectJAdvisorFactory must not be null");
//		this.beanFactory = beanFactory;
//		this.advisorFactory = advisorFactory;
//	}
//
//
//	/**
//	 * Look for AspectJ-annotated aspect beans in the current bean factory,
//	 * and return to a list of Spring AOP Advisors representing them.
//	 * <p>Creates a Spring Advisor for each AspectJ advice method.
//	 * @return the list of {@link org.springframework.aop.Advisor} beans
//	 * @see #isEligibleBean
//	 */
//	public List<Advisor> buildAspectJAdvisors() {
//		List<String> aspectNames = this.aspectBeanNames;
//
//		if (aspectNames == null) {
//			synchronized (this) {
//				aspectNames = this.aspectBeanNames;
//				if (aspectNames == null) {
//					List<Advisor> advisors = new LinkedList<>();
//					aspectNames = new LinkedList<>();
//					String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
//							this.beanFactory, Object.class, true, false);
//					for (String beanName : beanNames) {
//						if (!isEligibleBean(beanName)) {
//							continue;
//						}
//						// We must be careful not to instantiate beans eagerly as in this case they
//						// would be cached by the Spring container but would not have been weaved.
//						Class<?> beanType = this.beanFactory.getType(beanName);
//						if (beanType == null) {
//							continue;
//						}
//						if (this.advisorFactory.isAspect(beanType)) {
//							aspectNames.add(beanName);
//							AspectMetadata amd = new AspectMetadata(beanType, beanName);
//							if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
//								MetadataAwareAspectInstanceFactory factory =
//										new BeanFactoryAspectInstanceFactory(this.beanFactory, beanName);
//								List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);
//								if (this.beanFactory.isSingleton(beanName)) {
//									this.advisorsCache.put(beanName, classAdvisors);
//								}
//								else {
//									this.aspectFactoryCache.put(beanName, factory);
//								}
//								advisors.addAll(classAdvisors);
//							}
//							else {
//								// Per target or per this.
//								if (this.beanFactory.isSingleton(beanName)) {
//									throw new IllegalArgumentException("Bean with name '" + beanName +
//											"' is a singleton, but aspect instantiation model is not singleton");
//								}
//								MetadataAwareAspectInstanceFactory factory =
//										new PrototypeAspectInstanceFactory(this.beanFactory, beanName);
//								this.aspectFactoryCache.put(beanName, factory);
//								advisors.addAll(this.advisorFactory.getAdvisors(factory));
//							}
//						}
//					}
//					this.aspectBeanNames = aspectNames;
//					return advisors;
//				}
//			}
//		}
//
//		if (aspectNames.isEmpty()) {
//			return Collections.emptyList();
//		}
//		List<Advisor> advisors = new LinkedList<>();
//		for (String aspectName : aspectNames) {
//			List<Advisor> cachedAdvisors = this.advisorsCache.get(aspectName);
//			if (cachedAdvisors != null) {
//				advisors.addAll(cachedAdvisors);
//			}
//			else {
//				MetadataAwareAspectInstanceFactory factory = this.aspectFactoryCache.get(aspectName);
//				advisors.addAll(this.advisorFactory.getAdvisors(factory));
//			}
//		}
//		return advisors;
//	}
//
//	/**
//	 * Return whether the aspect bean with the given name is eligible.
//	 * @param beanName the name of the aspect bean
//	 * @return whether the bean is eligible
//	 */
//	protected boolean isEligibleBean(String beanName) {
//		return true;
//	}
//
//}