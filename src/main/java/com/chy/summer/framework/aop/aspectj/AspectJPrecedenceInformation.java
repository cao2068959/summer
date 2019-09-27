package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.core.Ordered;

/**
 * 该类型可以提供按AspectJ的优先规则对advice/advisors进行排序所需的信息。
 * 说白就是在排序的时候提供相应的信息
 */
public interface AspectJPrecedenceInformation extends Ordered {

	/**
	 * 获取切面bean的名称
	 */
	String getAspectName();

	/**
	 * 获取切面内部通知的顺序优先度
	 */
	int getDeclarationOrder();

	/**
	 * 判断是否为事前通知
	 */
	boolean isBeforeAdvice();

	/**
	 * 判断是否为事后通知
	 */
	boolean isAfterAdvice();

}