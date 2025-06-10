package com.chy.summer.framework.aop.aspectj.annotation;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.aopalliance.Advice;
import com.chy.summer.framework.aop.aspectj.AspectJExpressionPointcut;
import com.chy.summer.framework.aop.framework.AopConfigException;
import javax.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.List;

/**
 * AspectJ注释的advisor工厂
 * 这些工厂可以从使用AspectJ注释的类创建advisor
 */
public interface AspectJAdvisorFactory {

	/**
	 * 判断给定的类是否是切面
	 * 如果不是切面将会返回false，
	 * 如果是切面，但是summer aop无法处理这切面，则返回true，可以通过validate(Class<?> aspectClass)方法来判断是否可以处理
	 * 如果是切面，同时summer aop可以处理这切面返回true。
	 */
	boolean isAspect(Class<?> clazz);

	/**
	 * 给定的切面是否有效？
	 * @param aspectClass the supposed AspectJ annotation-style class to validate
	 * @throws AopConfigException 类是一个无效的aspect
	 * @throws NotAnAtAspectException 类根本不是aspect
	 */
	void validate(Class<?> aspectClass) throws AopConfigException;

	/**
	 * 为指定aspect实例上的所有带有AspectJ注释方法构建summer AOP advisor
	 * @param aspectInstanceFactory aspect实例工厂
	 * @return 该类的advisor列表
	 */
	List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory);

	/**
	 * 为给定的AspectJ advice方法构建一个summer AOP advisor
	 * @param candidateAdviceMethod 指定的advice方法
	 * @param aspectInstanceFactory aspect实例工厂
	 * @param declarationOrder aspect内的声明顺序
	 * @param aspectName 切面的名称
	 */
	@Nullable
	Advisor getAdvisor(Method candidateAdviceMethod, MetadataAwareAspectInstanceFactory aspectInstanceFactory,
					   int declarationOrder, String aspectName);

	/**
	 * 为给定的AspectJ Advice方法构建summer AOP Advice.
	 * @param candidateAdviceMethod 指定的advice方法
	 * @param expressionPointcut AspectJ切入点表达式
	 * @param aspectInstanceFactory aspect实例工厂
	 * @param declarationOrder aspect内的声明顺序
	 * @param aspectName 切面的名称
	 */
	@Nullable
	Advice getAdvice(Method candidateAdviceMethod, AspectJExpressionPointcut expressionPointcut,
					 MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName);

}