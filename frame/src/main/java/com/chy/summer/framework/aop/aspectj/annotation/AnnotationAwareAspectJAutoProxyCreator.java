package com.chy.summer.framework.aop.aspectj.annotation;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.beans.config.ListableBeanFactory;
import com.chy.summer.framework.util.Assert;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 根据Bean中的AspectJ注解自动创建代理
 */
public class AnnotationAwareAspectJAutoProxyCreator extends AspectJAwareAdvisorAutoProxyCreator {

	/**
	 * 正则表达式模式列表
	 */
	@Nullable
	private List<Pattern> includePatterns;

	@Nullable
	private AspectJAdvisorFactory aspectJAdvisorFactory;

	@Nullable
	private BeanFactoryAspectJAdvisorsBuilder aspectJAdvisorsBuilder;


	/**
	 * 设置一个正则表达式模式列表，匹配合格的@AspectJ bean名称
	 * 默认所有@AspectJ bean都视为合格
	 */
	public void setIncludePatterns(List<String> patterns) {
		this.includePatterns = new ArrayList<>(patterns.size());
		for (String patternText : patterns) {
			this.includePatterns.add(Pattern.compile(patternText));
		}
	}

	/**
	 * 设置aspectJAdvisorFactory
	 * @param aspectJAdvisorFactory
	 */
	public void setAspectJAdvisorFactory(AspectJAdvisorFactory aspectJAdvisorFactory) {
		Assert.notNull(aspectJAdvisorFactory, "AspectJAdvisorFactory不可为空");
		this.aspectJAdvisorFactory = aspectJAdvisorFactory;
	}

	/**
	 * 初始化bean工厂
	 * @param beanFactory
	 */
	@Override
	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		//初始化beanFactory，并且注册索引
		super.initBeanFactory(beanFactory);
		if (this.aspectJAdvisorFactory == null) {
			//创建一个反射式的aspectJAdvisorFactory
			this.aspectJAdvisorFactory = new ReflectiveAspectJAdvisorFactory(beanFactory);
		}
		this.aspectJAdvisorsBuilder =
				new BeanFactoryAspectJAdvisorsBuilderAdapter(beanFactory, this.aspectJAdvisorFactory);
	}

	/**
	 * 找到所有可以在自动代理中使用的候选Advisor
	 */
	@Override
	protected List<Advisor> findCandidateAdvisors() {
		// 根据父类规则找到的所有advisor。
		List<Advisor> advisors = super.findCandidateAdvisors();
		// 额外再添加Bean工厂中所有AspectJ切面构建的advisor
		if (this.aspectJAdvisorsBuilder != null) {
			advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
		}
		return advisors;
	}

	/**
	 * 返回给定的bean类是否表示不应代理的基础结构类
	 */
	@Override
	protected boolean isInfrastructureClass(Class<?> beanClass) {
		// 对父类的判断条件做扩充
		return (super.isInfrastructureClass(beanClass) ||
				(this.aspectJAdvisorFactory != null && this.aspectJAdvisorFactory.isAspect(beanClass)));
	}

	/**
	 * 检查给定的切面bean是否符合自动代理的条件
	 */
	protected boolean isEligibleAspectBean(String beanName) {
		if (this.includePatterns == null) {
			//默认情况下全部通过
			return true;
		}
		else {
			for (Pattern pattern : this.includePatterns) {
				//对每个代理条件做一次匹配
				if (pattern.matcher(beanName).matches()) {
					return true;
				}
			}
			return false;
		}
	}


	/**
	 * BeanFactoryAspectJAdvisorsBuilderAdapter的子类，重写isEligibleBean方法
	 */
	private class BeanFactoryAspectJAdvisorsBuilderAdapter extends BeanFactoryAspectJAdvisorsBuilder {

		/**
		 * 为给定的BeanFactory创建一个新的BeanFactoryAspectJAdvisorsBuilder
		 * @param beanFactory 用来扫描的ListableBeanFactory
		 * @param advisorFactory 用来构建Advisor的AspectJAdvisorFactory
		 */
		public BeanFactoryAspectJAdvisorsBuilderAdapter(
                ListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {

			super(beanFactory, advisorFactory);
		}

		/**
		 * Aspect Bean的名称是否合格
		 */
		@Override
		protected boolean isEligibleBean(String beanName) {
			return AnnotationAwareAspectJAutoProxyCreator.this.isEligibleAspectBean(beanName);
		}
	}

}