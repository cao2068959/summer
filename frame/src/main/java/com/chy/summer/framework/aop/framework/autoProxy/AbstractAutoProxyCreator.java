//package com.chy.summer.framework.aop.framework.autoProxy;
//
//import com.chy.summer.framework.aop.Advisor;
//import com.chy.summer.framework.aop.Pointcut;
//import com.chy.summer.framework.aop.TargetSource;
//import com.chy.summer.framework.aop.aopalliance.Advice;
//import com.chy.summer.framework.aop.framework.ProxyProcessorSupport;
//import com.chy.summer.framework.beans.BeanFactory;
//import com.chy.summer.framework.beans.BeanFactoryAware;
//import com.chy.summer.framework.beans.FactoryBean;
//import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
//import com.chy.summer.framework.util.StringUtils;
//import com.sun.istack.internal.Nullable;
//
//import java.beans.PropertyDescriptor;
//import java.lang.reflect.Constructor;
//import java.util.Collections;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
//public abstract class AbstractAutoProxyCreator extends ProxyProcessorSupport
//		implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware {
//
//	/**
//	 * Convenience constant for subclasses: Return value for "do not proxy".
//	 * @see #getAdvicesAndAdvisorsForBean
//	 */
//	@Nullable
//	protected static final Object[] DO_NOT_PROXY = null;
//
//	/**
//	 * Convenience constant for subclasses: Return value for
//	 * "proxy without additional interceptors, just the common ones".
//	 * @see #getAdvicesAndAdvisorsForBean
//	 */
//	protected static final Object[] PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS = new Object[0];
//
//
//	/** Logger available to subclasses */
//	protected final Log logger = LogFactory.getLog(getClass());
//
//	/** Default is global AdvisorAdapterRegistry */
//	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();
//
//	/**
//	 * Indicates whether or not the proxy should be frozen. Overridden from super
//	 * to prevent the configuration from becoming frozen too early.
//	 */
//	private boolean freezeProxy = false;
//
//	/** Default is no common interceptors */
//	private String[] interceptorNames = new String[0];
//
//	private boolean applyCommonInterceptorsFirst = true;
//
//	@Nullable
//	private TargetSourceCreator[] customTargetSourceCreators;
//
//	@Nullable
//	private BeanFactory beanFactory;
//
//	private final Set<String> targetSourcedBeans = Collections.newSetFromMap(new ConcurrentHashMap<>(16));
//
//	private final Set<Object> earlyProxyReferences = Collections.newSetFromMap(new ConcurrentHashMap<>(16));
//
//	private final Map<Object, Class<?>> proxyTypes = new ConcurrentHashMap<>(16);
//
//	private final Map<Object, Boolean> advisedBeans = new ConcurrentHashMap<>(256);
//
//
//	/**
//	 * Set whether or not the proxy should be frozen, preventing advice
//	 * from being added to it once it is created.
//	 * <p>Overridden from the super class to prevent the proxy configuration
//	 * from being frozen before the proxy is created.
//	 */
//	@Override
//	public void setFrozen(boolean frozen) {
//		this.freezeProxy = frozen;
//	}
//
//	@Override
//	public boolean isFrozen() {
//		return this.freezeProxy;
//	}
//
//	/**
//	 * Specify the {@link AdvisorAdapterRegistry} to use.
//	 * <p>Default is the global {@link AdvisorAdapterRegistry}.
//	 * @see org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry
//	 */
//	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
//		this.advisorAdapterRegistry = advisorAdapterRegistry;
//	}
//
//	/**
//	 * Set custom {@code TargetSourceCreators} to be applied in this order.
//	 * If the list is empty, or they all return null, a {@link SingletonTargetSource}
//	 * will be created for each bean.
//	 * <p>Note that TargetSourceCreators will kick in even for target beans
//	 * where no advices or advisors have been found. If a {@code TargetSourceCreator}
//	 * returns a {@link TargetSource} for a specific bean, that bean will be proxied
//	 * in any case.
//	 * <p>{@code TargetSourceCreators} can only be invoked if this post processor is used
//	 * in a {@link BeanFactory} and its {@link BeanFactoryAware} callback is triggered.
//	 * @param targetSourceCreators the list of {@code TargetSourceCreators}.
//	 * Ordering is significant: The {@code TargetSource} returned from the first matching
//	 * {@code TargetSourceCreator} (that is, the first that returns non-null) will be used.
//	 */
//	public void setCustomTargetSourceCreators(TargetSourceCreator... targetSourceCreators) {
//		this.customTargetSourceCreators = targetSourceCreators;
//	}
//
//	/**
//	 * Set the common interceptors. These must be bean names in the current factory.
//	 * They can be of any advice or advisor type Spring supports.
//	 * <p>If this property isn't set, there will be zero common interceptors.
//	 * This is perfectly valid, if "specific" interceptors such as matching
//	 * Advisors are all we want.
//	 */
//	public void setInterceptorNames(String... interceptorNames) {
//		this.interceptorNames = interceptorNames;
//	}
//
//	/**
//	 * Set whether the common interceptors should be applied before bean-specific ones.
//	 * Default is "true"; else, bean-specific interceptors will get applied first.
//	 */
//	public void setApplyCommonInterceptorsFirst(boolean applyCommonInterceptorsFirst) {
//		this.applyCommonInterceptorsFirst = applyCommonInterceptorsFirst;
//	}
//
//	@Override
//	public void setBeanFactory(BeanFactory beanFactory) {
//		this.beanFactory = beanFactory;
//	}
//
//	/**
//	 * Return the owning {@link BeanFactory}.
//	 * May be {@code null}, as this post-processor doesn't need to belong to a bean factory.
//	 */
//	@Nullable
//	protected BeanFactory getBeanFactory() {
//		return this.beanFactory;
//	}
//
//
//	@Override
//	@Nullable
//	public Class<?> predictBeanType(Class<?> beanClass, String beanName) {
//		if (this.proxyTypes.isEmpty()) {
//			return null;
//		}
//		Object cacheKey = getCacheKey(beanClass, beanName);
//		return this.proxyTypes.get(cacheKey);
//	}
//
//	@Override
//	@Nullable
//	public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeansException {
//		return null;
//	}
//
//	@Override
//	public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
//		Object cacheKey = getCacheKey(bean.getClass(), beanName);
//		if (!this.earlyProxyReferences.contains(cacheKey)) {
//			this.earlyProxyReferences.add(cacheKey);
//		}
//		return wrapIfNecessary(bean, beanName, cacheKey);
//	}
//
//	@Override
//	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
//		Object cacheKey = getCacheKey(beanClass, beanName);
//
//		if (!StringUtils.hasLength(beanName) || !this.targetSourcedBeans.contains(beanName)) {
//			if (this.advisedBeans.containsKey(cacheKey)) {
//				return null;
//			}
//			if (isInfrastructureClass(beanClass) || shouldSkip(beanClass, beanName)) {
//				this.advisedBeans.put(cacheKey, Boolean.FALSE);
//				return null;
//			}
//		}
//
//		// Create proxy here if we have a custom TargetSource.
//		// Suppresses unnecessary default instantiation of the target bean:
//		// The TargetSource will handle target instances in a custom fashion.
//		TargetSource targetSource = getCustomTargetSource(beanClass, beanName);
//		if (targetSource != null) {
//			if (StringUtils.hasLength(beanName)) {
//				this.targetSourcedBeans.add(beanName);
//			}
//			Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
//			Object proxy = createProxy(beanClass, beanName, specificInterceptors, targetSource);
//			this.proxyTypes.put(cacheKey, proxy.getClass());
//			return proxy;
//		}
//
//		return null;
//	}
//
//	@Override
//	public boolean postProcessAfterInstantiation(Object bean, String beanName) {
//		return true;
//	}
//
//	@Override
//	public PropertyValues postProcessPropertyValues(
//			PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) {
//
//		return pvs;
//	}
//
//	@Override
//	public Object postProcessBeforeInitialization(Object bean, String beanName) {
//		return bean;
//	}
//
//	/**
//	 * Create a proxy with the configured interceptors if the bean is
//	 * identified as one to proxy by the subclass.
//	 * @see #getAdvicesAndAdvisorsForBean
//	 */
//	@Override
//	public Object postProcessAfterInitialization(@Nullable Object bean, String beanName) throws BeansException {
//		if (bean != null) {
//			Object cacheKey = getCacheKey(bean.getClass(), beanName);
//			if (!this.earlyProxyReferences.contains(cacheKey)) {
//				return wrapIfNecessary(bean, beanName, cacheKey);
//			}
//		}
//		return bean;
//	}
//
//
//	/**
//	 * Build a cache key for the given bean class and bean name.
//	 * <p>Note: As of 4.2.3, this implementation does not return a concatenated
//	 * class/name String anymore but rather the most efficient cache key possible:
//	 * a plain bean name, prepended with {@link BeanFactory#FACTORY_BEAN_PREFIX}
//	 * in case of a {@code FactoryBean}; or if no bean name specified, then the
//	 * given bean {@code Class} as-is.
//	 * @param beanClass the bean class
//	 * @param beanName the bean name
//	 * @return the cache key for the given class and name
//	 */
//	protected Object getCacheKey(Class<?> beanClass, @Nullable String beanName) {
//		if (StringUtils.hasLength(beanName)) {
//			return (FactoryBean.class.isAssignableFrom(beanClass) ?
//					BeanFactory.FACTORY_BEAN_PREFIX + beanName : beanName);
//		}
//		else {
//			return beanClass;
//		}
//	}
//
//	/**
//	 * Wrap the given bean if necessary, i.e. if it is eligible for being proxied.
//	 * @param bean the raw bean instance
//	 * @param beanName the name of the bean
//	 * @param cacheKey the cache key for metadata access
//	 * @return a proxy wrapping the bean, or the raw bean instance as-is
//	 */
//	protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
//		if (StringUtils.hasLength(beanName) && this.targetSourcedBeans.contains(beanName)) {
//			return bean;
//		}
//		if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
//			return bean;
//		}
//		if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
//			this.advisedBeans.put(cacheKey, Boolean.FALSE);
//			return bean;
//		}
//
//		// Create proxy if we have advice.
//		Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
//		if (specificInterceptors != DO_NOT_PROXY) {
//			this.advisedBeans.put(cacheKey, Boolean.TRUE);
//			Object proxy = createProxy(
//					bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
//			this.proxyTypes.put(cacheKey, proxy.getClass());
//			return proxy;
//		}
//
//		this.advisedBeans.put(cacheKey, Boolean.FALSE);
//		return bean;
//	}
//
//	/**
//	 * Return whether the given bean class represents an infrastructure class
//	 * that should never be proxied.
//	 * <p>The default implementation considers Advices, Advisors and
//	 * AopInfrastructureBeans as infrastructure classes.
//	 * @param beanClass the class of the bean
//	 * @return whether the bean represents an infrastructure class
//	 * @see org.aopalliance.aop.Advice
//	 * @see org.springframework.aop.Advisor
//	 * @see org.springframework.aop.framework.AopInfrastructureBean
//	 * @see #shouldSkip
//	 */
//	protected boolean isInfrastructureClass(Class<?> beanClass) {
//		boolean retVal = Advice.class.isAssignableFrom(beanClass) ||
//				Pointcut.class.isAssignableFrom(beanClass) ||
//				Advisor.class.isAssignableFrom(beanClass) ||
//				AopInfrastructureBean.class.isAssignableFrom(beanClass);
//		if (retVal && logger.isTraceEnabled()) {
//			logger.trace("Did not attempt to auto-proxy infrastructure class [" + beanClass.getName() + "]");
//		}
//		return retVal;
//	}
//
//	/**
//	 * Subclasses should override this method to return {@code true} if the
//	 * given bean should not be considered for auto-proxying by this post-processor.
//	 * <p>Sometimes we need to be able to avoid this happening if it will lead to
//	 * a circular reference. This implementation returns {@code false}.
//	 * @param beanClass the class of the bean
//	 * @param beanName the name of the bean
//	 * @return whether to skip the given bean
//	 */
//	protected boolean shouldSkip(Class<?> beanClass, String beanName) {
//		return false;
//	}
//
//	/**
//	 * Create a target source for bean instances. Uses any TargetSourceCreators if set.
//	 * Returns {@code null} if no custom TargetSource should be used.
//	 * <p>This implementation uses the "customTargetSourceCreators" property.
//	 * Subclasses can override this method to use a different mechanism.
//	 * @param beanClass the class of the bean to create a TargetSource for
//	 * @param beanName the name of the bean
//	 * @return a TargetSource for this bean
//	 * @see #setCustomTargetSourceCreators
//	 */
//	@Nullable
//	protected TargetSource getCustomTargetSource(Class<?> beanClass, String beanName) {
//		// We can't create fancy target sources for directly registered singletons.
//		if (this.customTargetSourceCreators != null &&
//				this.beanFactory != null && this.beanFactory.containsBean(beanName)) {
//			for (TargetSourceCreator tsc : this.customTargetSourceCreators) {
//				TargetSource ts = tsc.getTargetSource(beanClass, beanName);
//				if (ts != null) {
//					// Found a matching TargetSource.
//					if (logger.isDebugEnabled()) {
//						logger.debug("TargetSourceCreator [" + tsc +
//								" found custom TargetSource for bean with name '" + beanName + "'");
//					}
//					return ts;
//				}
//			}
//		}
//
//		// No custom TargetSource found.
//		return null;
//	}
//
//	/**
//	 * Create an AOP proxy for the given bean.
//	 * @param beanClass the class of the bean
//	 * @param beanName the name of the bean
//	 * @param specificInterceptors the set of interceptors that is
//	 * specific to this bean (may be empty, but not null)
//	 * @param targetSource the TargetSource for the proxy,
//	 * already pre-configured to access the bean
//	 * @return the AOP proxy for the bean
//	 * @see #buildAdvisors
//	 */
//	protected Object createProxy(Class<?> beanClass, @Nullable String beanName,
//			@Nullable Object[] specificInterceptors, TargetSource targetSource) {
//
//		if (this.beanFactory instanceof ConfigurableListableBeanFactory) {
//			AutoProxyUtils.exposeTargetClass((ConfigurableListableBeanFactory) this.beanFactory, beanName, beanClass);
//		}
//
//		ProxyFactory proxyFactory = new ProxyFactory();
//		proxyFactory.copyFrom(this);
//
//		if (!proxyFactory.isProxyTargetClass()) {
//			if (shouldProxyTargetClass(beanClass, beanName)) {
//				proxyFactory.setProxyTargetClass(true);
//			}
//			else {
//				evaluateProxyInterfaces(beanClass, proxyFactory);
//			}
//		}
//
//		Advisor[] advisors = buildAdvisors(beanName, specificInterceptors);
//		proxyFactory.addAdvisors(advisors);
//		proxyFactory.setTargetSource(targetSource);
//		customizeProxyFactory(proxyFactory);
//
//		proxyFactory.setFrozen(this.freezeProxy);
//		if (advisorsPreFiltered()) {
//			proxyFactory.setPreFiltered(true);
//		}
//
//		return proxyFactory.getProxy(getProxyClassLoader());
//	}
//
//	/**
//	 * Determine whether the given bean should be proxied with its target class rather than its interfaces.
//	 * <p>Checks the {@link AutoProxyUtils#PRESERVE_TARGET_CLASS_ATTRIBUTE "preserveTargetClass" attribute}
//	 * of the corresponding bean definition.
//	 * @param beanClass the class of the bean
//	 * @param beanName the name of the bean
//	 * @return whether the given bean should be proxied with its target class
//	 * @see AutoProxyUtils#shouldProxyTargetClass
//	 */
//	protected boolean shouldProxyTargetClass(Class<?> beanClass, @Nullable String beanName) {
//		return (this.beanFactory instanceof ConfigurableListableBeanFactory &&
//				AutoProxyUtils.shouldProxyTargetClass((ConfigurableListableBeanFactory) this.beanFactory, beanName));
//	}
//
//	/**
//	 * Return whether the Advisors returned by the subclass are pre-filtered
//	 * to match the bean's target class already, allowing the ClassFilter check
//	 * to be skipped when building advisors chains for AOP invocations.
//	 * <p>Default is {@code false}. Subclasses may override this if they
//	 * will always return pre-filtered Advisors.
//	 * @return whether the Advisors are pre-filtered
//	 * @see #getAdvicesAndAdvisorsForBean
//	 * @see org.springframework.aop.framework.Advised#setPreFiltered
//	 */
//	protected boolean advisorsPreFiltered() {
//		return false;
//	}
//
//	/**
//	 * Determine the advisors for the given bean, including the specific interceptors
//	 * as well as the common interceptor, all adapted to the Advisor interface.
//	 * @param beanName the name of the bean
//	 * @param specificInterceptors the set of interceptors that is
//	 * specific to this bean (may be empty, but not null)
//	 * @return the list of Advisors for the given bean
//	 */
//	protected Advisor[] buildAdvisors(@Nullable String beanName, @Nullable Object[] specificInterceptors) {
//		// Handle prototypes correctly...
//		Advisor[] commonInterceptors = resolveInterceptorNames();
//
//		List<Object> allInterceptors = new ArrayList<>();
//		if (specificInterceptors != null) {
//			allInterceptors.addAll(Arrays.asList(specificInterceptors));
//			if (commonInterceptors.length > 0) {
//				if (this.applyCommonInterceptorsFirst) {
//					allInterceptors.addAll(0, Arrays.asList(commonInterceptors));
//				}
//				else {
//					allInterceptors.addAll(Arrays.asList(commonInterceptors));
//				}
//			}
//		}
//		if (logger.isDebugEnabled()) {
//			int nrOfCommonInterceptors = commonInterceptors.length;
//			int nrOfSpecificInterceptors = (specificInterceptors != null ? specificInterceptors.length : 0);
//			logger.debug("Creating implicit proxy for bean '" + beanName + "' with " + nrOfCommonInterceptors +
//					" common interceptors and " + nrOfSpecificInterceptors + " specific interceptors");
//		}
//
//		Advisor[] advisors = new Advisor[allInterceptors.size()];
//		for (int i = 0; i < allInterceptors.size(); i++) {
//			advisors[i] = this.advisorAdapterRegistry.wrap(allInterceptors.get(i));
//		}
//		return advisors;
//	}
//
//	/**
//	 * Resolves the specified interceptor names to Advisor objects.
//	 * @see #setInterceptorNames
//	 */
//	private Advisor[] resolveInterceptorNames() {
//		Assert.state(this.beanFactory != null, "BeanFactory required for resolving interceptor names");
//		ConfigurableBeanFactory cbf = (this.beanFactory instanceof ConfigurableBeanFactory ?
//				(ConfigurableBeanFactory) this.beanFactory : null);
//		List<Advisor> advisors = new ArrayList<>();
//		for (String beanName : this.interceptorNames) {
//			if (cbf == null || !cbf.isCurrentlyInCreation(beanName)) {
//				Object next = this.beanFactory.getBean(beanName);
//				advisors.add(this.advisorAdapterRegistry.wrap(next));
//			}
//		}
//		return advisors.toArray(new Advisor[advisors.size()]);
//	}
//
//	/**
//	 * Subclasses may choose to implement this: for example,
//	 * to change the interfaces exposed.
//	 * <p>The default implementation is empty.
//	 * @param proxyFactory ProxyFactory that is already configured with
//	 * TargetSource and interfaces and will be used to create the proxy
//	 * immediately after this method returns
//	 */
//	protected void customizeProxyFactory(ProxyFactory proxyFactory) {
//	}
//
//
//	/**
//	 * Return whether the given bean is to be proxied, what additional
//	 * advices (e.g. AOP Alliance interceptors) and advisors to apply.
//	 * @param beanClass the class of the bean to advise
//	 * @param beanName the name of the bean
//	 * @param customTargetSource the TargetSource returned by the
//	 * {@link #getCustomTargetSource} method: may be ignored.
//	 * Will be {@code null} if no custom target source is in use.
//	 * @return an array of additional interceptors for the particular bean;
//	 * or an empty array if no additional interceptors but just the common ones;
//	 * or {@code null} if no proxy at all, not even with the common interceptors.
//	 * See constants DO_NOT_PROXY and PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS.
//	 * @throws BeansException in case of errors
//	 * @see #DO_NOT_PROXY
//	 * @see #PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS
//	 */
//	@Nullable
//	protected abstract Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName,
//			@Nullable TargetSource customTargetSource) throws BeansException;
//
//}