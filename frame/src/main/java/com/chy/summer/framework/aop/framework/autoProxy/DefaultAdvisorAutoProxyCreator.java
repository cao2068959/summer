package com.chy.summer.framework.aop.framework.autoProxy;

import com.chy.summer.framework.beans.BeanNameAware;
import javax.annotation.Nullable;

/**
 * 基于当前BeanFactory中的所有候选advisor创建AOP代理的BeanPostProcessor实现
 * 这个类是完全通用的;它不包含处理任何特定方面的特殊代码，例如池化方面。
 */
public class DefaultAdvisorAutoProxyCreator extends AbstractAdvisorAutoProxyCreator implements BeanNameAware {

	/** Bean名称的前缀和其余部分之间的分隔符 */
	public final static String SEPARATOR = ".";

	private boolean usePrefix = false;

	@Nullable
	private String advisorBeanNamePrefix;


	/**
	 * 设置是否仅使用bean名称中包括特定前缀的advisor
	 */
	public void setUsePrefix(boolean usePrefix) {
		this.usePrefix = usePrefix;
	}

	/**
	 * 返回是否在Bean名称中仅包括具有特定前缀的Advisor
	 */
	public boolean isUsePrefix() {
		return this.usePrefix;
	}

	/**
	 * 设置bean名称的前缀，以使它们包含在此对象的自动代理中
	 * 应该设置此前缀以避免循环引用 默认值是这个对象的beanName+“.”。
	 * @param advisorBeanNamePrefix the exclusion prefix
	 */
	public void setAdvisorBeanNamePrefix(@Nullable String advisorBeanNamePrefix) {
		this.advisorBeanNamePrefix = advisorBeanNamePrefix;
	}

	/**
	 * 获取bean名称的前缀
	 */
	@Nullable
	public String getAdvisorBeanNamePrefix() {
		return this.advisorBeanNamePrefix;
	}

	@Override
	public void setBeanName(String name) {
		// 如果未设置基础结构bean名称前缀，将会覆盖掉前缀
		if (this.advisorBeanNamePrefix == null) {
			this.advisorBeanNamePrefix = name + SEPARATOR;
		}
	}


	/**
	 * 通过前缀判断Advisor Bean是否合格的。
	 */
	@Override
	protected boolean isEligibleAdvisorBean(String beanName) {
		if (!isUsePrefix()) {
			return true;
		}
		String prefix = getAdvisorBeanNamePrefix();
		return (prefix != null && beanName.startsWith(prefix));
	}

}