//package com.chy.summer.framework.aop.framework.autoProxy;
//
//import com.chy.summer.framework.aop.Advisor;
//import com.chy.summer.framework.aop.Pointcut;
//import com.chy.summer.framework.aop.TargetSource;
//import com.chy.summer.framework.aop.aopalliance.Advice;
//import com.chy.summer.framework.aop.framework.AopInfrastructureBean;
//import com.chy.summer.framework.aop.framework.ProxyFactory;
//import com.chy.summer.framework.aop.framework.ProxyProcessorSupport;
//import com.chy.summer.framework.aop.framework.adapter.AdvisorAdapterRegistry;
//import com.chy.summer.framework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
//import com.chy.summer.framework.aop.target.SingletonTargetSource;
//import com.chy.summer.framework.beans.*;
//import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
//import com.chy.summer.framework.beans.config.SmartInstantiationAwareBeanPostProcessor;
//import com.chy.summer.framework.exception.BeansException;
//import com.chy.summer.framework.util.Assert;
//import com.chy.summer.framework.util.StringUtils;
//import com.sun.istack.internal.Nullable;
//
//import java.beans.PropertyDescriptor;
//import java.lang.reflect.Constructor;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * BeanPostProcessor的实现，该实现使用AOP代理包装每个合格的bean，并在调用bean本身之前委派给指定的拦截器
// * 这个类区分了“公共”拦截器和“特定”拦截器，前者用于它创建的所有代理，后者用于每个bean实例。
// * 不需要任何通用的拦截器。如果存在，则使用interceptorNames属性设置它们。与一样ProxyFactoryBean，使用当前工厂中的拦截器名称（而不是bean引用）来正确处理原型advisor和拦截器
// * 如果有大量的bean需要用类似的代理(即委托给相同的拦截器)来包装，那么这种自动代理特别有用。
// * 子类可以应用任何策略来决定一个bean是否被代理，例如通过类型、名称、定义细节等。它们还可以返回应该只应用于特定bean实例的其他拦截器。
// *
// * 可以使用任意数量的TargetSourceCreator实现来创建自定义目标源:例如，来共享原型对象。只要TargetSourceCreator指定了自定义TargetSource，即使没有advice，也会发生自动代理。如果没有设置TargetSourceCreators，或者没有匹配，那么默认情况下将使用一个SingletonTargetSource包装目标bean实例。
// */
//public abstract class AbstractAutoProxyCreator extends ProxyProcessorSupport
//		implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware {
//
//	/**
//	 * 不需要代理的对象
//	 */
//	@Nullable
//	protected static final Object[] DO_NOT_PROXY = null;
//
//	/**
//	 * 没有额外拦截器的代理，只有常见拦截器的对象
//	 */
//	protected static final Object[] PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS = new Object[0];
//
//
////	/** Logger available to subclasses */
////	protected final Log logger = LogFactory.getLog(getClass());
//
//	/**
//     * advisor适配器注册表
//     */
//	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();
//
//	/**
//	 * 指示是否应冻结代理
//     * 从super覆盖，以防止配置过早冻结。
//	 */
//	private boolean freezeProxy = false;
//
//	/**
//     * 默认为没有通用拦截器
//     */
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
//	 * 设置是否应冻结代理，以防止在创建代理后向其添加建议。
//     * 重写父类，以防止在创建代理之前冻结代理配置。
//	 */
//	@Override
//	public void setFrozen(boolean frozen) {
//		this.freezeProxy = frozen;
//	}
//
//    /**
//     * 获取配置是否冻结，并且无法进行任何advice更改
//     */
//	@Override
//	public boolean isFrozen() {
//		return this.freezeProxy;
//	}
//
//	/**
//	 * 指定要使用的AdvisorAdapterRegistry（advisor适配器注册表)
//	 */
//	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
//		this.advisorAdapterRegistry = advisorAdapterRegistry;
//	}
//
//	/**
//	 * 设置顺序应用的TargetSourceCreators列表
//     * 如果列表为空，或者它们都返回null， 则将为每个bean创建一个SingletonTargetSource
//     * 即使没有找到advice或advisor的目标bean，TargetSourceCreators也会启动。
//     * 如果是一个特定的bean TargetSourceCreator会返回TargetSource， 在任何情况下都将对该bean进行代理
//     *
//     * TargetSourceCreators仅当在中使用此后处理器BeanFactory并BeanFactoryAware触发其回调时才能调用
//	 */
//	public void setCustomTargetSourceCreators(TargetSourceCreator... targetSourceCreators) {
//		this.customTargetSourceCreators = targetSourceCreators;
//	}
//
//	/**
//	 * 设置公共拦截器，必须是当前工厂中的bean名称，它们可以是summer支持的任何advice或advisor类型。
//     *
//     * 如果未设置此属性，则将没有公共拦截器。 如果我们需要“特定的”拦截器（例如匹配的Advisor），则这是完全没有关系的
//	 */
//	public void setInterceptorNames(String... interceptorNames) {
//		this.interceptorNames = interceptorNames;
//	}
//
//	/**
//	 * 设置是否应该在特定于bean的拦截器之前应用公共拦截器
//     * 默认是“true”;否则，将首先应用特定于bean的拦截器。
//	 */
//	public void setApplyCommonInterceptorsFirst(boolean applyCommonInterceptorsFirst) {
//		this.applyCommonInterceptorsFirst = applyCommonInterceptorsFirst;
//	}
//
//    /**
//     * 设定bean工厂
//     */
//	@Override
//	public void setBeanFactory(BeanFactory beanFactory) {
//		this.beanFactory = beanFactory;
//	}
//
//	/**
//	 * 获取持有的bean工厂
//	 */
//	@Nullable
//	protected BeanFactory getBeanFactory() {
//		return this.beanFactory;
//	}
//
//    /**
//     * 预测Bean的类型，返回第一个预测成功的Class类型，如果不能预测返回null
//     */
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
//    /**
//     * 选择合适的构造器，比如目标对象有多个构造器，在这里可以进行一些定制化，选择合适的构造器
//     * beanClass参数表示目标实例的类型，beanName是目标实例在容器中的name
//     * 返回值是个构造器数组，如果返回null，会执行下一个PostProcessor的determineCandidateConstructors方法；否则选取该PostProcessor选择的构造器
//     */
//	@Override
//	@Nullable
//	public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeansException {
//		return null;
//	}
//
//    /**
//     * 获得提前暴露的bean引用。主要用于解决循环引用的问题
//     * 只有单例对象才会调用此方法
//     */
//	@Override
//	public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
//		Object cacheKey = getCacheKey(bean.getClass(), beanName);
//		if (!this.earlyProxyReferences.contains(cacheKey)) {
//			this.earlyProxyReferences.add(cacheKey);
//		}
//		//判断是否需要被代理，如果需要则代理这个bean
//		return wrapIfNecessary(bean, beanName, cacheKey);
//	}
//
//    /**
//     * 通过构造函数或工厂方法在实例化bean之后,但在发生属性填充（通过显式属性或自动装配）之前执行操作。
//     */
//	@Override
//	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
//	    //获取缓存的key
//		Object cacheKey = getCacheKey(beanClass, beanName);
//
//		if (!StringUtils.hasLength(beanName) || !this.targetSourcedBeans.contains(beanName)) {
//		    //有些对象是不可以被代理的，基类：Advices, Advisors and AopInfrastructureBeans; 带有aop注解类: @Aspect
//            //子类可以复写该类,如果一些情况不需要被代理, shouldSkip方法返回true
//			if (this.advisedBeans.containsKey(cacheKey)) {
//				return null;
//			}
//			if (isInfrastructureClass(beanClass) || shouldSkip(beanClass, beanName)) {
//				this.advisedBeans.put(cacheKey, Boolean.FALSE);
//				return null;
//			}
//		}
//
//        //获取targetSource, 如果存在则直接在对象初始化之前进行创建代理, 避免了目标对象不必要的实例化
//		TargetSource targetSource = getCustomTargetSource(beanClass, beanName);
//		if (targetSource != null) {
//            //如果有自定义targetSource就要这里创建代理对象
//            //这样做的好处是被代理的对象可以动态改变，而不是值针对一个target对象(可以对对象池中对象进行代理，可以每次创建代理都创建新对象)
//			if (StringUtils.hasLength(beanName)) {
//				this.targetSourcedBeans.add(beanName);
//			}
//            //获取Advisors, 这个是交给子类实现的
//			Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
//			Object proxy = createProxy(beanClass, beanName, specificInterceptors, targetSource);
//			this.proxyTypes.put(cacheKey, proxy.getClass());
//			return proxy;
//		}
//
//		return null;
//	}
//
//    /**
//     * 在实例化目标bean之前应用此BeanPostProcessor 。
//     */
//	@Override
//	public boolean postProcessAfterInstantiation(Object bean, String beanName) {
//		return true;
//	}
//
//    /**
//     * 在工厂将它们应用于给定bean之前，对给定的属性值进行后处理，而无需使用属性描述符。
//     */
//	@Override
//	public PropertyValues postProcessPropertyValues(
//			PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) {
//
//		return pvs;
//	}
//
//    /**
//     * 初始化前 执行
//     */
//	@Override
//	public Object postProcessBeforeInitialization(Object bean, String beanName) {
//		return bean;
//	}
//
//    /**
//     * 初始化后执行
//     */
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
//     * 为给定的bean类和bean名称构建一个缓存键。
//     * 如果是FactoryBean，则以FACTORY_BEAN_PREFIX常量开头； 如果未指定bean名称，则按原样给定bean类
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
//	 * 必要时包装给定的bean
//	 */
//	protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
//        //targetSourcedBeans存在，说明前面创建过
//		if (StringUtils.hasLength(beanName) && this.targetSourcedBeans.contains(beanName)) {
//			return bean;
//		}
//		if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
//			return bean;
//		}
//        //有些对象是不可以被代理的，基类：Advices, Advisors and AopInfrastructureBeans; 带有aop注解类: @Aspect
//        //子类可以复写该类,如果一些情况不需要被代理, shouldSkip返回true
//		if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
//			this.advisedBeans.put(cacheKey, Boolean.FALSE);
//			return bean;
//		}
//
//		// 获取advisor, 寻找Advisors，过滤，并做排序
//		Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
//		if (specificInterceptors != DO_NOT_PROXY) {
//			this.advisedBeans.put(cacheKey, Boolean.TRUE);
//            //这边跟前面创建代理对象一样，只是默认用SingletonTargetSource
//			Object proxy = createProxy(
//					bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
//			//key与代理类型映射
//			this.proxyTypes.put(cacheKey, proxy.getClass());
//			return proxy;
//		}
//		this.advisedBeans.put(cacheKey, Boolean.FALSE);
//		return bean;
//	}
//
//	/**
//	 * 返回给定的bean类是否表示不应代理的基础结构类
//     * 默认实现将Advices，Advisor和AopInfrastructureBeans视为基础结构类
//	 */
//	protected boolean isInfrastructureClass(Class<?> beanClass) {
//		boolean retVal = Advice.class.isAssignableFrom(beanClass) ||
//				Pointcut.class.isAssignableFrom(beanClass) ||
//				Advisor.class.isAssignableFrom(beanClass) ||
//				AopInfrastructureBean.class.isAssignableFrom(beanClass);
////		if (retVal && logger.isTraceEnabled()) {
////			logger.trace("Did not attempt to auto-proxy infrastructure class [" + beanClass.getName() + "]");
////		}
//		return retVal;
//	}
//
//	/**
//	 * 如果该后处理器不考虑给定的bean自动代理，则子类应重写此方法以返回true。
//     * 如果有时导致循环引用，就必须能够避免这种情况的发生,此实现返回false。
//	 */
//	protected boolean shouldSkip(Class<?> beanClass, String beanName) {
//		return false;
//	}
//
//	/**
//	 * 为bean实例创建目标源。 如果已设置，则使用任何TargetSourceCreators。如果不应使用自定义TargetSource，则返回null。
//	 */
//	@Nullable
//	protected TargetSource getCustomTargetSource(Class<?> beanClass, String beanName) {
//		// 无法为直接注册的单例创建特殊的目标源。
//		if (this.customTargetSourceCreators != null &&
//				this.beanFactory != null && this.beanFactory.containsBean(beanName)) {
//			for (TargetSourceCreator tsc : this.customTargetSourceCreators) {
//				TargetSource ts = tsc.getTargetSource(beanClass, beanName);
//				if (ts != null) {
//					// 找到一个匹配的TargetSource
////					if (logger.isDebugEnabled()) {
////						logger.debug("TargetSourceCreator [" + tsc +
////								" found custom TargetSource for bean with name '" + beanName + "'");
////					}
//					return ts;
//				}
//			}
//		}
//
//		//没有找到匹配的TargetSource
//		return null;
//	}
//
//	/**
//	 * 为给定的bean创建一个aop代理
//	 * @param beanClass bean的类型
//	 * @param beanName bean的名称
//	 * @param specificInterceptors 特定于此bean的一组拦截器（可以为空，但不能为null）
//	 * @param targetSource 代理的TargetSource，已预先配置为可访问的Bean
//	 */
//	protected Object createProxy(Class<?> beanClass, @Nullable String beanName,
//			@Nullable Object[] specificInterceptors, TargetSource targetSource) {
//      TODO GYX 写到这里
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