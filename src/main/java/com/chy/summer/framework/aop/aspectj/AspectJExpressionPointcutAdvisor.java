package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.aop.Pointcut;
import com.chy.summer.framework.aop.support.AbstractGenericPointcutAdvisor;
import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.BeanFactoryAware;
import com.sun.istack.internal.Nullable;

/**
 * 可以用于任何AspectJ切入点表达式的AOP Advisor。
 */
public class AspectJExpressionPointcutAdvisor extends AbstractGenericPointcutAdvisor implements BeanFactoryAware {

	/**
	 * 切入点
	 */
	private final AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();

	/**
	 * 设置切入点表达式
	 */
	public void setExpression(@Nullable String expression) {
		this.pointcut.setExpression(expression);
	}

	/**
	 * 获取切入点表达式
	 */
	@Nullable
	public String getExpression() {
		return this.pointcut.getExpression();
	}

	/**
	 * 设置切入点位置
	 */
	public void setLocation(@Nullable String location) {
		this.pointcut.setLocation(location);
	}

	/**
	 * 获取切入点表达式
	 */
	@Nullable
	public String getLocation() {
		return this.pointcut.getLocation();
	}

	/**
	 * 设置切入点的参数名称
	 */
	public void setParameterNames(String... names) {
		this.pointcut.setParameterNames(names);
	}

	/**
	 * 设置切入点的参数类型
	 */
	public void setParameterTypes(Class<?>... types) {
		this.pointcut.setParameterTypes(types);
	}

	/**
	 * 设置bean工厂
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.pointcut.setBeanFactory(beanFactory);
	}

	/**
	 * 获取切入点
	 */
	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}

}