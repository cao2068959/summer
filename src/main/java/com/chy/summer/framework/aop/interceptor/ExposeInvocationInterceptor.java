package com.chy.summer.framework.aop.interceptor;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.chy.summer.framework.aop.support.DefaultPointcutAdvisor;
import com.chy.summer.framework.core.NamedThreadLocal;
import com.chy.summer.framework.core.PriorityOrdered;

import java.io.Serializable;

/**
 * 将当前的MethodInvocation暴露个线程局部对象的拦截器。
 * 除非确实必要，否则不要使用此拦截器。 目标对象通常不应该了解AOP，因为这会导致对summer API的依赖。
 * 目标对象应尽可能是普通POJO。
 */
public class ExposeInvocationInterceptor implements MethodInterceptor, PriorityOrdered, Serializable {

	/**
	 * 单例的instance
	 */
	public static final ExposeInvocationInterceptor INSTANCE = new ExposeInvocationInterceptor();

	/**
	 * 此类的单例advisor。 使用AOP时优先使用INSTANCE，因为它避免了创建新Advisor来包装的实例。
	 */
	public static final Advisor ADVISOR = new DefaultPointcutAdvisor(INSTANCE) {
		@Override
		public String toString() {
			return ExposeInvocationInterceptor.class.getName() +".ADVISOR";
		}
	};

	private static final ThreadLocal<MethodInvocation> invocation = new NamedThreadLocal<>("当前的AOP方法调用");


	/**
	 * 获得当前调用的AOP关联的MethodInvocation对象
	 */
	public static MethodInvocation currentInvocation() throws IllegalStateException {
		MethodInvocation mi = invocation.get();
		if (mi == null) {
			throw new IllegalStateException(
					"未找到MethodInvocation：检查AOP调用是否正在进行中，并且ExposeInvocationInterceptor在拦截器链中位于最前面。");
		}
		return mi;
	}


	/**
	 * 确保实例能够规范的创建
	 */
	private ExposeInvocationInterceptor() {
	}

	/**
	 * 调用指定的方法执行器
	 * @param mi 方法执行器
	 */
	@Override
	public Object invoke(MethodInvocation mi) throws Throwable {
		//从线程中获取原来的方法执行器
		MethodInvocation oldInvocation = invocation.get();
		//将新的方法执行器绑定到当前线程中
		invocation.set(mi);
		try {
			//执行方法
			return mi.proceed();
		}
		finally {
			//最后要把原来的方法执行器绑定回去
			invocation.set(oldInvocation);
		}
	}

	/**
	 * 获取执行优先度，永远是第二优先级
	 * @return
	 */
	@Override
	public int getOrder() {
		return PriorityOrdered.HIGHEST_PRECEDENCE + 1;
	}

	/**
	 * 在反序列化时替换为规范实例，以保护Singleton模式。
	 */
	private Object readResolve() {
		return INSTANCE;
	}

}