package com.chy.summer.framework.aop;

import com.chy.summer.framework.aop.aopalliance.Advice;

/**
 * AOP Alliance的Advice的子接口，允许附加接口（aop的规范）
 */
public interface DynamicIntroductionAdvice extends Advice {

	/**
	 * 判断是否实现了指定的接口
	 * @param intf 需要检查的接口
	 */
	boolean implementsInterface(Class<?> intf);

}