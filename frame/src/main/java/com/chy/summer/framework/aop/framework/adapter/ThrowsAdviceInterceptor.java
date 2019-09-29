package com.chy.summer.framework.aop.framework.adapter;

import com.chy.summer.framework.aop.AfterAdvice;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.chy.summer.framework.util.Assert;
import com.sun.istack.internal.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * ThrowsAdvice拦截器,持有ThrowsAdvice的实例对象
 */
public class ThrowsAdviceInterceptor implements MethodInterceptor, AfterAdvice {

	private static final String AFTER_THROWING = "afterThrowing";

//	private static final Logger logger = Logger.getLogger(ThrowsAdviceInterceptor.class);

	/**
	 * 持有的ThrowsAdvice实例
	 */
	private final Object throwsAdvice;

	/**
	 * 抛出advice的方法，以异常类为键
	 */
	private final Map<Class<?>, Method> exceptionHandlerMap = new HashMap<>();


	/**
	 * 为给定的ThrowsAdvice创建一个新的ThrowsAdviceInterceptor
	 */
	public ThrowsAdviceInterceptor(Object throwsAdvice) {
		Assert.notNull(throwsAdvice, "Advice不可为空");
		this.throwsAdvice = throwsAdvice;

		//获取异常通知中所有的方法
		Method[] methods = throwsAdvice.getClass().getMethods();
		for (Method method : methods) {
			//找到方法名叫做AFTER_THROWING的，并且他的参数是1个或者4个，他的最后一个参数是继承自Throwable
			if (method.getName().equals(AFTER_THROWING) &&
					(method.getParameterCount() == 1 || method.getParameterCount() == 4) &&
					Throwable.class.isAssignableFrom(method.getParameterTypes()[method.getParameterCount() - 1])
				) {
				// 缓存这个异常处理器
				this.exceptionHandlerMap.put(method.getParameterTypes()[method.getParameterCount() - 1], method);
//				if (logger.isDebugEnabled()) {
//					logger.debug("Found exception handler method: " + method);
//				}
			}
		}

		if (this.exceptionHandlerMap.isEmpty()) {
			throw new IllegalArgumentException(
					"在[" + throwsAdvice.getClass() + "]类中必须至少存在一个处理异常的方法");
		}
	}

	/**
	 * 获取处理异常方法的个数
	 */
	public int getHandlerMethodCount() {
		return this.exceptionHandlerMap.size();
	}

	/**
	 * 获取异常处理方法。 如果找不到，可以返回null。
	 */
	@Nullable
	private Method getExceptionHandler(Throwable exception) {
		Class<?> exceptionClass = exception.getClass();
//		if (logger.isTraceEnabled()) {
//			logger.trace("Trying to find handler for exception of type [" + exceptionClass.getName() + "]");
//		}
		//找到对应异常的处理器
		Method handler = this.exceptionHandlerMap.get(exceptionClass);
		while (handler == null && exceptionClass != Throwable.class) {
			//没有找到的话，就查询这个异常的父类的处理方法
			exceptionClass = exceptionClass.getSuperclass();
			handler = this.exceptionHandlerMap.get(exceptionClass);
		}
//		if (handler != null && logger.isDebugEnabled()) {
//			logger.debug("Found handler for exception of type [" + exceptionClass.getName() + "]: " + handler);
//		}
		return handler;
	}

	/**
	 * 执行ThrowsAdvice的方法，然后返回链中下一个对象
	 */
	@Override
	public Object invoke(MethodInvocation mi) throws Throwable {
		try {
			return mi.proceed();
		}
		catch (Throwable ex) {
			//获取这个异常的处理方式
			Method handlerMethod = getExceptionHandler(ex);
			if (handlerMethod != null) {
				//执行这个异常的处理方法
				invokeHandlerMethod(mi, ex, handlerMethod);
			}
			throw ex;
		}
	}

	/**
	 * 执行异常处理方法
	 */
	private void invokeHandlerMethod(MethodInvocation mi, Throwable ex, Method method) throws Throwable {
		Object[] handlerArgs;
		//获取异常处理方法的参数
		if (method.getParameterCount() == 1) {
			//一个参数的
			handlerArgs = new Object[] { ex };
		}
		else {
			//三个参数的
			handlerArgs = new Object[] {mi.getMethod(), mi.getArguments(), mi.getThis(), ex};
		}
		try {
			//执行
			method.invoke(this.throwsAdvice, handlerArgs);
		}
		catch (InvocationTargetException targetEx) {
			throw targetEx.getTargetException();
		}
	}

}