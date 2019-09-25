package com.chy.summer.framework.aop.interceptor;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;

import java.io.Serializable;

public class ExposeInvocationInterceptor {
//public class ExposeInvocationInterceptor implements MethodInterceptor, PriorityOrdered, Serializable {
	//TODO 我太难了 这个又没写完
	/** Singleton instance of this class */
	public static final ExposeInvocationInterceptor INSTANCE = new ExposeInvocationInterceptor();

	/**
	 * Singleton advisor for this class. Use in preference to INSTANCE when using
	 * Spring AOP, as it prevents the need to create a new Advisor to wrap the instance.
	 */
//	public static final Advisor ADVISOR = new DefaultPointcutAdvisor(INSTANCE) {
//		@Override
//		public String toString() {
//			return ExposeInvocationInterceptor.class.getName() +".ADVISOR";
//		}
//	};
//
//	private static final ThreadLocal<MethodInvocation> invocation =
//			new NamedThreadLocal<>("Current AOP method invocation");


	/**
	 * Return the AOP Alliance MethodInvocation object associated with the current invocation.
	 * @return the invocation object associated with the current invocation
	 * @throws IllegalStateException if there is no AOP invocation in progress,
	 * or if the ExposeInvocationInterceptor was not added to this interceptor chain
	 */
	public static MethodInvocation currentInvocation() throws IllegalStateException {
//		MethodInvocation mi = invocation.get();
//		if (mi == null)
//			throw new IllegalStateException(
//					"No MethodInvocation found: Check that an AOP invocation is in progress, and that the " +
//					"ExposeInvocationInterceptor is upfront in the interceptor chain. Specifically, note that " +
//					"advices with order HIGHEST_PRECEDENCE will execute before ExposeInvocationInterceptor!");
//		return mi;
		return null;
	}


	/**
	 * Ensures that only the canonical instance can be created.
	 */
	private ExposeInvocationInterceptor() {
	}

//	@Override
//	public Object invoke(MethodInvocation mi) throws Throwable {
//		MethodInvocation oldInvocation = invocation.get();
//		invocation.set(mi);
//		try {
//			return mi.proceed();
//		}
//		finally {
//			invocation.set(oldInvocation);
//		}
//	}
//
//	@Override
//	public int getOrder() {
//		return PriorityOrdered.HIGHEST_PRECEDENCE + 1;
//	}

	/**
	 * Required to support serialization. Replaces with canonical instance
	 * on deserialization, protecting Singleton pattern.
	 * <p>Alternative to overriding the {@code equals} method.
	 */
	private Object readResolve() {
		return INSTANCE;
	}

}