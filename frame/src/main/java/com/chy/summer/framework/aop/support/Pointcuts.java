package com.chy.summer.framework.aop.support;

import com.chy.summer.framework.aop.MethodMatcher;
import com.chy.summer.framework.aop.Pointcut;
import com.chy.summer.framework.util.Assert;
import com.sun.istack.internal.Nullable;

import java.io.Serializable;
import java.lang.reflect.Method;

public abstract class Pointcuts {

	/** 在任何类中匹配所有bean属性setter的切入点 */
	public static final Pointcut SETTERS = SetterPointcut.INSTANCE;

	/** 在任何类中匹配所有bean属性getter的切入点 */
	public static final Pointcut GETTERS = GetterPointcut.INSTANCE;


	/**
	 * 匹配所有给定切入点匹配的方法.（并集）
	 * @param pc1 切入点1
	 * @param pc2 切入点2
	 */
	public static Pointcut union(Pointcut pc1, Pointcut pc2) {
		return new ComposablePointcut(pc1).union(pc2);
	}

	/**
	 * 匹配所有给定切入点都匹配的所有方法(交集)
	 * @param pc1 切入点1
	 * @param pc2 切入点2
	 */
	public static Pointcut intersection(Pointcut pc1, Pointcut pc2) {
		return new ComposablePointcut(pc1).intersection(pc2);
	}

	/**
	 * 对切入点匹配执行最简单的检查。
	 * @param pointcut 匹配的切入点
	 * @param method 候选的方法
	 * @param targetClass 目标类
	 * @param args 方法参数
	 */
	public static boolean matches(Pointcut pointcut, Method method, Class<?> targetClass, Object... args) {
		Assert.notNull(pointcut, "Pointcut不可为空");
		if (pointcut == Pointcut.TRUE) {
			return true;
		}
		if (pointcut.getClassFilter().matches(targetClass)) {
			//仅检查它是否通过了一次
			MethodMatcher mm = pointcut.getMethodMatcher();
			if (mm.matches(method, targetClass)) {
				// 执行
				return (!mm.isRuntime() || mm.matches(method, targetClass, args));
			}
		}
		return false;
	}


	/**
	 * 匹配bean属性setter的切入点实现
	 */
	private static class SetterPointcut extends StaticMethodMatcherPointcut implements Serializable {

		public static SetterPointcut INSTANCE = new SetterPointcut();

		@Override
		public boolean matches(Method method, @Nullable Class<?> targetClass) {
			//方法名是set开头，并且有一个参数 没有返回值
			return (method.getName().startsWith("set") &&
					method.getParameterCount() == 1 &&
					method.getReturnType() == Void.TYPE);
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}


	/**
	 * 匹配bean属性getter的切入点实现
	 */
	private static class GetterPointcut extends StaticMethodMatcherPointcut implements Serializable {

		public static GetterPointcut INSTANCE = new GetterPointcut();

		@Override
		public boolean matches(Method method, @Nullable Class<?> targetClass) {
			//方法名是get开头，并且没有参数
			return (method.getName().startsWith("get") &&
					method.getParameterCount() == 0);
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}

}