//package com.chy.summer.framework.aop.aspectj.annotation;
//
//import com.chy.summer.framework.aop.Advisor;
//import com.chy.summer.framework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
//import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
//import com.chy.summer.framework.beans.config.ListableBeanFactory;
//import com.chy.summer.framework.util.Assert;
//import com.sun.istack.internal.Nullable;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Pattern;
//
//public class AnnotationAwareAspectJAutoProxyCreator extends AspectJAwareAdvisorAutoProxyCreator {
//
//	@Nullable
//	private List<Pattern> includePatterns;
//
//	@Nullable
//	private AspectJAdvisorFactory aspectJAdvisorFactory;
//
//	@Nullable
//	private BeanFactoryAspectJAdvisorsBuilder aspectJAdvisorsBuilder;
//
//
//	/**
//	 * Set a list of regex patterns, matching eligible @AspectJ bean names.
//	 * <p>Default is to consider all @AspectJ beans as eligible.
//	 */
//	public void setIncludePatterns(List<String> patterns) {
//		this.includePatterns = new ArrayList<>(patterns.size());
//		for (String patternText : patterns) {
//			this.includePatterns.add(Pattern.compile(patternText));
//		}
//	}
//
//	public void setAspectJAdvisorFactory(AspectJAdvisorFactory aspectJAdvisorFactory) {
//		Assert.notNull(aspectJAdvisorFactory, "AspectJAdvisorFactory must not be null");
//		this.aspectJAdvisorFactory = aspectJAdvisorFactory;
//	}
//
//	@Override
//	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
//		super.initBeanFactory(beanFactory);
//		if (this.aspectJAdvisorFactory == null) {
//			this.aspectJAdvisorFactory = new ReflectiveAspectJAdvisorFactory(beanFactory);
//		}
//		this.aspectJAdvisorsBuilder =
//				new BeanFactoryAspectJAdvisorsBuilderAdapter(beanFactory, this.aspectJAdvisorFactory);
//	}
//
//
//	@Override
//	protected List<Advisor> findCandidateAdvisors() {
//		// Add all the Spring advisors found according to superclass rules.
//		List<Advisor> advisors = super.findCandidateAdvisors();
//		// Build Advisors for all AspectJ aspects in the bean factory.
//		if (this.aspectJAdvisorsBuilder != null) {
//			advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
//		}
//		return advisors;
//	}
//
//	@Override
//	protected boolean isInfrastructureClass(Class<?> beanClass) {
//		// Previously we setProxyTargetClass(true) in the constructor, but that has too
//		// broad an impact. Instead we now override isInfrastructureClass to avoid proxying
//		// aspects. I'm not entirely happy with that as there is no good reason not
//		// to advise aspects, except that it causes advice invocation to go through a
//		// proxy, and if the aspect implements e.g the Ordered interface it will be
//		// proxied by that interface and fail at runtime as the advice method is not
//		// defined on the interface. We could potentially relax the restriction about
//		// not advising aspects in the future.
//		return (super.isInfrastructureClass(beanClass) ||
//				(this.aspectJAdvisorFactory != null && this.aspectJAdvisorFactory.isAspect(beanClass)));
//	}
//
//	/**
//	 * Check whether the given aspect bean is eligible for auto-proxying.
//	 * <p>If no &lt;aop:include&gt; elements were used then "includePatterns" will be
//	 * {@code null} and all beans are included. If "includePatterns" is non-null,
//	 * then one of the patterns must match.
//	 */
//	protected boolean isEligibleAspectBean(String beanName) {
//		if (this.includePatterns == null) {
//			return true;
//		}
//		else {
//			for (Pattern pattern : this.includePatterns) {
//				if (pattern.matcher(beanName).matches()) {
//					return true;
//				}
//			}
//			return false;
//		}
//	}
//
//
//	/**
//	 * Subclass of BeanFactoryAspectJAdvisorsBuilderAdapter that delegates to
//	 * surrounding AnnotationAwareAspectJAutoProxyCreator facilities.
//	 */
//	private class BeanFactoryAspectJAdvisorsBuilderAdapter extends BeanFactoryAspectJAdvisorsBuilder {
//
//		public BeanFactoryAspectJAdvisorsBuilderAdapter(
//                ListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {
//
//			super(beanFactory, advisorFactory);
//		}
//
//		@Override
//		protected boolean isEligibleBean(String beanName) {
//			return AnnotationAwareAspectJAutoProxyCreator.this.isEligibleAspectBean(beanName);
//		}
//	}
//
//}