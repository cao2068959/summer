package com.chy.summer.framework.aop.support;

import com.chy.summer.framework.aop.Pointcut;
import javax.annotation.Nullable;

/**
 * 使用字符串表达式的切入点的接口
 */
public interface ExpressionPointcut extends Pointcut {

	/**
	 * 获取这个切入点的字符串表达式
	 */
	@Nullable
	String getExpression();
}
