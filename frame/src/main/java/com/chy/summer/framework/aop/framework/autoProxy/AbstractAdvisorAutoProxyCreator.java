package com.chy.summer.framework.aop.framework.autoProxy;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.TargetSource;
import com.chy.summer.framework.aop.framework.ProxyCreationContext;
import com.chy.summer.framework.aop.support.AopUtils;
import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.core.ordered.AnnotationAwareOrderComparator;
import com.chy.summer.framework.util.Assert;
import com.sun.istack.internal.Nullable;

import java.util.List;

public abstract class AbstractAdvisorAutoProxyCreator extends AbstractAutoProxyCreator {

	/**
	 * 在自动代理时,用于从BeanFactory检索标准的Advisor的帮助类
	 */
	@Nullable
	private BeanFactoryAdvisorRetrievalHelper advisorRetrievalHelper;


	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		super.setBeanFactory(beanFactory);
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalArgumentException("AdvisorAutoProxyCreator需要一个ConfigurableListableBeanFactory: " + beanFactory);
		}
		initBeanFactory((ConfigurableListableBeanFactory) beanFactory);
	}

	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		this.advisorRetrievalHelper = new BeanFactoryAdvisorRetrievalHelperAdapter(beanFactory);
	}

	/**
	 * 获取Bean的Advices和Advisors
	 */
	@Override
	@Nullable
	protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, @Nullable TargetSource targetSource) {
		List<Advisor> advisors = findEligibleAdvisors(beanClass, beanName);
		if (advisors.isEmpty()) {
			//不要代理
			return DO_NOT_PROXY;
		}
		return advisors.toArray();
	}

	/**
	 * 找到所有合适的advisor自动代理这个类。
	 * @param beanClass bean的类型
	 * @param beanName 当前代理的bean的名称
	 */
	protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
		//找到所有的可以用于自动代理的advisor
		List<Advisor> candidateAdvisors = findCandidateAdvisors();
		//找到可以应用于指定bean的所有advisor
		List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName);
		//子类进行扩展
		extendAdvisors(eligibleAdvisors);
		if (!eligibleAdvisors.isEmpty()) {
			//对advisor进行排序
			eligibleAdvisors = sortAdvisors(eligibleAdvisors);
		}
		return eligibleAdvisors;
	}

	/**
	 * 找到所有可以在自动代理中使用的候选Advisor
	 */
	protected List<Advisor> findCandidateAdvisors() {
		Assert.state(this.advisorRetrievalHelper != null, "没有可用的BeanFactoryAdvisorRetrievalHelper");
		return this.advisorRetrievalHelper.findAdvisorBeans();
	}

	/**
	 * 搜索给定的候选advisor，以找到可以应用于指定bean的所有advisor
	 * @param candidateAdvisors 候选Advisor
	 * @param beanClass 目标的bean类型
	 * @param beanName 目标的bean名称
	 */
	protected List<Advisor> findAdvisorsThatCanApply(
			List<Advisor> candidateAdvisors, Class<?> beanClass, String beanName) {

		ProxyCreationContext.setCurrentProxiedBeanName(beanName);
		try {
			return AopUtils.findAdvisorsThatCanApply(candidateAdvisors, beanClass);
		}
		finally {
			ProxyCreationContext.setCurrentProxiedBeanName(null);
		}
	}

	/**
	 * 返回具有给定名称的Advisor Bean是否首先适合代理
	 * @param beanName Advisor bean的名称
	 */
	protected boolean isEligibleAdvisorBean(String beanName) {
		return true;
	}

	/**
	 * 根据顺序对Advisor进行排序，子类可以选择重写此方法以自定义排序策略
	 * @param advisors Advisor的源列表
	 */
	protected List<Advisor> sortAdvisors(List<Advisor> advisors) {
		AnnotationAwareOrderComparator.sort(advisors);
		return advisors;
	}

	/**
	 * 子类通过重新这个方法进行扩展，用于注册额外的advisor
	 * @param candidateAdvisors 已被标识为应用于给定bean的Advisor
	 */
	protected void extendAdvisors(List<Advisor> candidateAdvisors) {
	}

	/**
	 * 此自动代理创建器一直返回预过滤的Advisors.
	 */
	@Override
	protected boolean advisorsPreFiltered() {
		return true;
	}


	/**
	 * BeanFactoryAdvisorRetrievalHelper的子类，委派给周围的AbstractAdvisorAutoProxyCreator工具
	 */
	private class BeanFactoryAdvisorRetrievalHelperAdapter extends BeanFactoryAdvisorRetrievalHelper {

		public BeanFactoryAdvisorRetrievalHelperAdapter(ConfigurableListableBeanFactory beanFactory) {
			super(beanFactory);
		}

		/**
		 * 判断bean是否合格
		 * 默认实现始终为true
		 */
		@Override
		protected boolean isEligibleBean(String beanName) {
			return AbstractAdvisorAutoProxyCreator.this.isEligibleAdvisorBean(beanName);
		}
	}

}