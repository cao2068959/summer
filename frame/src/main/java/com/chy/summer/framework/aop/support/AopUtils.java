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
}