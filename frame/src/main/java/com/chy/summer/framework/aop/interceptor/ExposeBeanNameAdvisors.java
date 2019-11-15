package com.chy.summer.framework.aop.interceptor;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.ProxyMethodInvocation;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.chy.summer.framework.aop.support.DefaultIntroductionAdvisor;
import com.chy.summer.framework.aop.support.DefaultPointcutAdvisor;
import com.chy.summer.framework.aop.support.DelegatingIntroductionInterceptor;
import com.chy.summer.framework.beans.NamedBean;

/**
 * 在使用IoC容器创建bean并将bean名称绑定到当前调用时，可以使用方便的方法来创建顾问。
 * 可以用AspectJ支持bean()切入点指示器
 */
public abstract class ExposeBeanNameAdvisors {

	/**
	 * 绑定当前在ReflectiveMethodInvocation userAttributes映射中调用的bean的bean名称
	 */
	private static final String BEAN_NAME_ATTRIBUTE = ExposeBeanNameAdvisors.class.getName() + ".BEAN_NAME";


	/**
	 * 查找当前调用的bean名称。
     * 假定拦截器链中已经包含ExposeBeanNameAdvisor，并且该调用是通过ExposeInvocationInterceptor公开的
	 */
	public static String getBeanName() throws IllegalStateException {
		return getBeanName(ExposeInvocationInterceptor.currentInvocation());
	}

	/**
	 * 查找给定调用的bean名称。 假定ExposeBeanNameAdvisor已包含在拦截器链中
	 * @param mi 应该包含bean名称作为属性的MethodInvocation
	 */
	public static String getBeanName(MethodInvocation mi) throws IllegalStateException {
		if (!(mi instanceof ProxyMethodInvocation)) {
			throw new IllegalArgumentException("MethodInvocation不是ProxyMethodInvocation: " + mi);
		}
		ProxyMethodInvocation pmi = (ProxyMethodInvocation) mi;
		//获取bean名称
		String beanName = (String) pmi.getUserAttribute(BEAN_NAME_ATTRIBUTE);
		if (beanName == null) {
			throw new IllegalStateException("无法获得bean名称; 未在MethodInvocation上设置: " + mi);
		}
		return beanName;
	}

	/**
     * 创建一个新的advisor，该advisor将公开给定的bean名称，但是没有introduction
	 * @param beanName 暴露的bean名称
	 */
	public static Advisor createAdvisorWithoutIntroduction(String beanName) {
		return new DefaultPointcutAdvisor(new ExposeBeanNameInterceptor(beanName));
	}

	/**
	 * 创建一个新的advisor工具，它将公开给定的bean名称，引入NamedBean接口使bean名称可访问，
     * 而不必强制目标对象知道这个IoC概念。
	 * @param beanName 要暴露的bean名称
	 */
	public static Advisor createAdvisorIntroducingNamedBean(String beanName) {
		return new DefaultIntroductionAdvisor(new ExposeBeanNameIntroduction(beanName));
	}


	/**
	 * 将指定的bean名称公开为调用属性的拦截器
	 */
	private static class ExposeBeanNameInterceptor implements MethodInterceptor {

		private final String beanName;
        //设置名称的构造器
		public ExposeBeanNameInterceptor(String beanName) {
			this.beanName = beanName;
		}

        /**
         * 执行方法
         * 在执行方法前，设置bean的名称
         * @param mi 方法拦截器
         */
		@Override
		public Object invoke(MethodInvocation mi) throws Throwable {
			if (!(mi instanceof ProxyMethodInvocation)) {
				throw new IllegalStateException("MethodInvocation不是ProxyMethodInvocation: " + mi);
			}
			ProxyMethodInvocation pmi = (ProxyMethodInvocation) mi;
			//设置bean的名称
			pmi.setUserAttribute(BEAN_NAME_ATTRIBUTE, this.beanName);
			return mi.proceed();
		}
	}


	/**
	 * 将指定的bean名称公开为调用属性的介绍
	 */
	private static class ExposeBeanNameIntroduction extends DelegatingIntroductionInterceptor implements NamedBean {

		private final String beanName;

		public ExposeBeanNameIntroduction(String beanName) {
			this.beanName = beanName;
		}

		@Override
		public Object invoke(MethodInvocation mi) throws Throwable {
			if (!(mi instanceof ProxyMethodInvocation)) {
				throw new IllegalStateException("MethodInvocation不是ProxyMethodInvocation: " + mi);
			}
			ProxyMethodInvocation pmi = (ProxyMethodInvocation) mi;
			pmi.setUserAttribute(BEAN_NAME_ATTRIBUTE, this.beanName);
			return super.invoke(mi);
		}

		@Override
		public String getBeanName() {
			return this.beanName;
		}
	}

}