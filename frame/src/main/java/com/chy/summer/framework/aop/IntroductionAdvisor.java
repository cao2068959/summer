package com.chy.summer.framework.aop;

/**
 * IntroductionAdvisors的根接口
 */
public interface IntroductionAdvisor extends Advisor, IntroductionInfo {

	/**
	 * 获取类过滤器，确定此Introduction应适用于哪些目标类。
	 */
	ClassFilter getClassFilter();

	/**
	 * 判断Advisor能否通过IntroductionAdvisor来实现
	 * 在添加导入advisor之前调用
	 */
	void validateInterfaces() throws IllegalArgumentException;

}