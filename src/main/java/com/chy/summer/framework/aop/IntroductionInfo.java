package com.chy.summer.framework.aop;

/**
 * 提供所需接口信息的接口。
 */
public interface IntroductionInfo {

	/**
	 * 返回此Advisor或者Advice引入的其他接口。
	 */
	Class<?>[] getInterfaces();
}