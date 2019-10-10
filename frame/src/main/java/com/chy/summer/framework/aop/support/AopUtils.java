package com.chy.summer.framework.aop.support;

import com.chy.summer.framework.aop.*;
import com.chy.summer.framework.core.BridgeMethodResolver;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.ReflectionUtils;
import com.sun.istack.internal.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * aop工具类
 */
public abstract class AopUtils {
	/**
	 * 检查给定的对象是JDK动态代理或是CGLIB代理。
	 * 此方法还检查给定对象是否为SummerProxy的实例。
	 */
	public static boolean isAopProxy(@Nullable Object object) {
		return (object instanceof SummerProxy &&
				(Proxy.isProxyClass(object.getClass()) || ClassUtils.isCglibProxyClass(object.getClass())));
	}

	/**
	 * 检查给定的对象是不是CGLIB代理。
	 * 此方法还检查给定对象是否为SummerProxy的实例。
	 */
	public static boolean isCglibProxy(@Nullable Object object) {
		return (object instanceof SummerProxy && ClassUtils.isCglibProxy(object));
	}

	/**
	 * 根据给定的方法，在指定的目标类中找到相应的方法，这个方法可能来自一个接口。
	 * 在AOP调用的时候也可以找到相应的目标方法
	 */
	public static Method getMostSpecificMethod(Method method, @Nullable Class<?> targetClass) {
		//获得具体的方法
		Method resolvedMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
		// 如果方法参数具有泛型，就需要获取到原始方法。
		return BridgeMethodResolver.findBridgedMethod(resolvedMethod);
	}

	/**
	 * 判断给定方法是否为“equals”方法
	 */
	public static boolean isEqualsMethod(@Nullable Method method) {
		return ReflectionUtils.isEqualsMethod(method);
	}

	/**
	 * 判断给定方法是否为“hashCode”方法
	 */
	public static boolean isHashCodeMethod(@Nullable Method method) {
		return ReflectionUtils.isHashCodeMethod(method);
	}

	/**
	 * 确定给定的方法是否是Object.finalize()方法
	 */
	public static boolean isFinalizeMethod(@Nullable Method method) {
		return (method != null && method.getName().equals("finalize") &&
				method.getParameterCount() == 0);
	}

	/**
	 * 通过反射调用给定目标，AOP调用方法的一部分代码
	 * @param target 目标对象
	 * @param method 调用的方法
	 * @param args 方法的参数
	 */
	@Nullable
	public static Object invokeJoinpointUsingReflection(@Nullable Object target, Method method, Object[] args)
			throws Throwable {

		// 使用反射来调用该方法
		try {
			//将需要调用的方法设置成可访问
			ReflectionUtils.makeAccessible(method);
			return method.invoke(target, args);
		}
		catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
		catch (IllegalArgumentException ex) {
			throw new AopInvocationException("AOP配置似乎无效: 尝试执行 [" + target + "]中的[" + method + "]方法", ex);
		}
		catch (IllegalAccessException ex) {
			throw new AopInvocationException("无法访问 [" + method + "]方法", ex);
		}
	}

	/**
	 * 确定在指定的Advisor列表中适用于给定类的Advisor
	 * @param candidateAdvisors 需要评估的advisor
	 * @param clazz 目标类型
	 */
	public static List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> clazz) {
		if (candidateAdvisors.isEmpty()) {
			return candidateAdvisors;
		}
		//用来存放适合的advisor
		List<Advisor> eligibleAdvisors = new LinkedList<>();
		for (Advisor candidate : candidateAdvisors) {
			//找到
			if (candidate instanceof IntroductionAdvisor && canApply(candidate, clazz)) {
				eligibleAdvisors.add(candidate);
			}
		}
		boolean hasIntroductions = !eligibleAdvisors.isEmpty();
		for (Advisor candidate : candidateAdvisors) {
			if (candidate instanceof IntroductionAdvisor) {
				// already processed
				continue;
			}
			if (canApply(candidate, clazz, hasIntroductions)) {
				eligibleAdvisors.add(candidate);
			}
		}
		return eligibleAdvisors;
	}

	/**
	 * 给定的advisor能否应用于给定的类?
	 * 可以用来优化一个类的advisor
	 * @param advisor 需要检查的advisor
	 * @param targetClass 指定的测试类
	 * @return 切入点是否可以应用于任何方法
	 */
	public static boolean canApply(Advisor advisor, Class<?> targetClass) {
		return canApply(advisor, targetClass, false);
	}

	/**
	 * 给定的切入点可以完全应用于给定的类吗？
	 * @param pc 要检查的静态或动态切入点
	 * @param targetClass 指定的测试类
	 * @param hasIntroductions 这个bean的advisor链是否包含introductionAdvisor
	 */
	public static boolean canApply(Pointcut pc, Class<?> targetClass, boolean hasIntroductions) {
		Assert.notNull(pc, "Pointcut不可为空");
		if (!pc.getClassFilter().matches(targetClass)) {
			return false;
		}
		//获取切入点的方法匹配器
		MethodMatcher methodMatcher = pc.getMethodMatcher();
		if (methodMatcher == MethodMatcher.TRUE) {
			// 默认的方法匹配器可以匹配所有方法
			return true;
		}
		//TODO GYX 写到这里
		IntroductionAwareMethodMatcher introductionAwareMethodMatcher = null;
		if (methodMatcher instanceof IntroductionAwareMethodMatcher) {
			introductionAwareMethodMatcher = (IntroductionAwareMethodMatcher) methodMatcher;
		}

		Set<Class<?>> classes = new LinkedHashSet<>(ClassUtils.getAllInterfacesForClassAsSet(targetClass));
		classes.add(targetClass);
		for (Class<?> clazz : classes) {
			Method[] methods = ReflectionUtils.getAllDeclaredMethods(clazz);
			for (Method method : methods) {
				if ((introductionAwareMethodMatcher != null &&
						introductionAwareMethodMatcher.matches(method, targetClass, hasIntroductions)) ||
						methodMatcher.matches(method, targetClass)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 给定的advisor能否应用于给定的类?
	 * @param advisor 需要检查的advisor
	 * @param targetClass 指定的测试类
	 * @param hasIntroductions 这个bean的advisor链是否包含introductionAdvisor
	 */
	public static boolean canApply(Advisor advisor, Class<?> targetClass, boolean hasIntroductions) {
		//IntroductionAdvisor直接使用类匹配器进行匹配就行了
		if (advisor instanceof IntroductionAdvisor) {
			return ((IntroductionAdvisor) advisor).getClassFilter().matches(targetClass);
		}
		//PointcutAdvisor类型的需要更复杂的判断
		else if (advisor instanceof PointcutAdvisor) {
			PointcutAdvisor pca = (PointcutAdvisor) advisor;
			return canApply(pca.getPointcut(), targetClass, hasIntroductions);
		}
		else {
			// 如果没有切入点，就当做它适用
			return true;
		}
	}
}