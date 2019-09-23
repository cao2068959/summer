package com.chy.summer.framework.aop;

/**
 * 切点控制器，适用于除了introduction之外的所有advisors
 */
public interface PointcutAdvisor extends Advisor {

	/**
	 * 获取切点
	 */
	Pointcut getPointcut();

}
