package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.aop.ClassFilter;
import com.chy.summer.framework.aop.IntroductionAdvisor;
import com.chy.summer.framework.aop.aopalliance.Advice;
import com.chy.summer.framework.aop.support.ClassFilters;
import com.chy.summer.framework.aop.support.DelegatePerTargetObjectIntroductionInterceptor;
import com.chy.summer.framework.aop.support.DelegatingIntroductionInterceptor;

/**
 * DeclareParents的顾问
 * DeclareParents主要的作用是在已有的类中添加新得方法
 * 其原理为需要添加的方法建立一个类，然后建一个代理类，同时代理该方法类和需要增方法的目标类
 */
public class DeclareParentsAdvisor implements IntroductionAdvisor {
	/**
	 * introduced的接口类型
	 */
	private final Class<?> introducedInterface;

	private final ClassFilter typePatternClassFilter;

	private final Advice advice;


	/**
	 * 为此DeclareParents创建一个新的Advisor。
	 * @param interfaceType 定义introduction的静态字段
	 * @param typePattern 类型模式
	 * @param defaultImpl 默认的实现类
	 */
	public DeclareParentsAdvisor(Class<?> interfaceType, String typePattern, Class<?> defaultImpl) {
		this(interfaceType, typePattern, defaultImpl,
			 new DelegatePerTargetObjectIntroductionInterceptor(defaultImpl, interfaceType));
	}

	/**
	 * 为此DeclareParents创建一个新的Advisor。
	 * @param interfaceType 定义introduction的静态字段
	 * @param typePattern 类型模式
	 * @param delegateRef 委托实现对象
	 */
	public DeclareParentsAdvisor(Class<?> interfaceType, String typePattern, Object delegateRef) {
		this(interfaceType, typePattern, delegateRef.getClass(),
			 new DelegatingIntroductionInterceptor(delegateRef));
	}

	/**
	 * 私有的构造方法，用于上面两个构造方法的公用代码
	 * 由于使用了final字段，因此无法使用普通方法来共享通用代码
	 * @param interfaceType 定义introduction的静态字段
	 * @param typePattern 类型模式
	 * @param implementationClass 实现类
	 * @param advice delegation通知
	 */
	private DeclareParentsAdvisor(Class<?> interfaceType, String typePattern, Class<?> implementationClass, Advice advice) {
		//设置introduced的接口类型
		this.introducedInterface = interfaceType;
		//初始化类型过滤器
		ClassFilter typePatternFilter = new TypePatternClassFilter(typePattern);

		// ClassFilter上有@FunctionalInterface注解，在使用ClassFilter.matches方法的时候会执行下面的lambda表达式
		ClassFilter exclusion = clazz -> !(introducedInterface.isAssignableFrom(clazz));
		//将两个过滤条件合并
		this.typePatternClassFilter = ClassFilters.intersection(typePatternFilter, exclusion);
		//持有advice
		this.advice = advice;
	}

	/**
	 * 获取类过滤器
	 */
	@Override
	public ClassFilter getClassFilter() {
		return this.typePatternClassFilter;
	}

	@Override
	public void validateInterfaces() throws IllegalArgumentException {
		// Do nothing
	}

	@Override
	public boolean isPerInstance() {
		return true;
	}

	/**
	 * 获取通知
	 */
	@Override
	public Advice getAdvice() {
		return this.advice;
	}

	/**
	 * 获取接口类型
	 */
	@Override
	public Class<?>[] getInterfaces() {
		return new Class<?>[] {this.introducedInterface};
	}

}