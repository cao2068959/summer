//package com.chy.summer.framework.aop.framework.autoProxy;
//
//public abstract class AbstractAdvisorAutoProxyCreator extends AbstractAutoProxyCreator {
//
//	@Nullable
//	private BeanFactoryAdvisorRetrievalHelper advisorRetrievalHelper;
//
//
//	@Override
//	public void setBeanFactory(BeanFactory beanFactory) {
//		super.setBeanFactory(beanFactory);
//		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
//			throw new IllegalArgumentException(
//					"AdvisorAutoProxyCreator requires a ConfigurableListableBeanFactory: " + beanFactory);
//		}
//		initBeanFactory((ConfigurableListableBeanFactory) beanFactory);
//	}
//
//	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
//		this.advisorRetrievalHelper = new BeanFactoryAdvisorRetrievalHelperAdapter(beanFactory);
//	}
//
//
//	@Override
//	@Nullable
//	protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, @Nullable TargetSource targetSource) {
//		List<Advisor> advisors = findEligibleAdvisors(beanClass, beanName);
//		if (advisors.isEmpty()) {
//			return DO_NOT_PROXY;
//		}
//		return advisors.toArray();
//	}
//
//	/**
//	 * Find all eligible Advisors for auto-proxying this class.
//	 * @param beanClass the clazz to find advisors for
//	 * @param beanName the name of the currently proxied bean
//	 * @return the empty List, not {@code null},
//	 * if there are no pointcuts or interceptors
//	 * @see #findCandidateAdvisors
//	 * @see #sortAdvisors
//	 * @see #extendAdvisors
//	 */
//	protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
//		List<Advisor> candidateAdvisors = findCandidateAdvisors();
//		List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName);
//		extendAdvisors(eligibleAdvisors);
//		if (!eligibleAdvisors.isEmpty()) {
//			eligibleAdvisors = sortAdvisors(eligibleAdvisors);
//		}
//		return eligibleAdvisors;
//	}
//
//	/**
//	 * Find all candidate Advisors to use in auto-proxying.
//	 * @return the List of candidate Advisors
//	 */
//	protected List<Advisor> findCandidateAdvisors() {
//		Assert.state(this.advisorRetrievalHelper != null, "No BeanFactoryAdvisorRetrievalHelper available");
//		return this.advisorRetrievalHelper.findAdvisorBeans();
//	}
//
//	/**
//	 * Search the given candidate Advisors to find all Advisors that
//	 * can apply to the specified bean.
//	 * @param candidateAdvisors the candidate Advisors
//	 * @param beanClass the target's bean class
//	 * @param beanName the target's bean name
//	 * @return the List of applicable Advisors
//	 * @see ProxyCreationContext#getCurrentProxiedBeanName()
//	 */
//	protected List<Advisor> findAdvisorsThatCanApply(
//			List<Advisor> candidateAdvisors, Class<?> beanClass, String beanName) {
//
//		ProxyCreationContext.setCurrentProxiedBeanName(beanName);
//		try {
//			return AopUtils.findAdvisorsThatCanApply(candidateAdvisors, beanClass);
//		}
//		finally {
//			ProxyCreationContext.setCurrentProxiedBeanName(null);
//		}
//	}
//
//	/**
//	 * Return whether the Advisor bean with the given name is eligible
//	 * for proxying in the first place.
//	 * @param beanName the name of the Advisor bean
//	 * @return whether the bean is eligible
//	 */
//	protected boolean isEligibleAdvisorBean(String beanName) {
//		return true;
//	}
//
//	/**
//	 * Sort advisors based on ordering. Subclasses may choose to override this
//	 * method to customize the sorting strategy.
//	 * @param advisors the source List of Advisors
//	 * @return the sorted List of Advisors
//	 * @see org.springframework.core.Ordered
//	 * @see org.springframework.core.annotation.Order
//	 * @see org.springframework.core.annotation.AnnotationAwareOrderComparator
//	 */
//	protected List<Advisor> sortAdvisors(List<Advisor> advisors) {
//		AnnotationAwareOrderComparator.sort(advisors);
//		return advisors;
//	}
//
//	/**
//	 * Extension hook that subclasses can override to register additional Advisors,
//	 * given the sorted Advisors obtained to date.
//	 * <p>The default implementation is empty.
//	 * <p>Typically used to add Advisors that expose contextual information
//	 * required by some of the later advisors.
//	 * @param candidateAdvisors Advisors that have already been identified as
//	 * applying to a given bean
//	 */
//	protected void extendAdvisors(List<Advisor> candidateAdvisors) {
//	}
//
//	/**
//	 * This auto-proxy creator always returns pre-filtered Advisors.
//	 */
//	@Override
//	protected boolean advisorsPreFiltered() {
//		return true;
//	}
//
//
//	/**
//	 * Subclass of BeanFactoryAdvisorRetrievalHelper that delegates to
//	 * surrounding AbstractAdvisorAutoProxyCreator facilities.
//	 */
//	private class BeanFactoryAdvisorRetrievalHelperAdapter extends BeanFactoryAdvisorRetrievalHelper {
//
//		public BeanFactoryAdvisorRetrievalHelperAdapter(ConfigurableListableBeanFactory beanFactory) {
//			super(beanFactory);
//		}
//
//		@Override
//		protected boolean isEligibleBean(String beanName) {
//			return AbstractAdvisorAutoProxyCreator.this.isEligibleAdvisorBean(beanName);
//		}
//	}
//
//}