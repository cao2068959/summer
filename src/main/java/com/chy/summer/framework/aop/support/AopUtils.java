package com.chy.summer.framework.aop.support;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.IntroductionAdvisor;
import com.chy.summer.framework.aop.PointcutAdvisor;
import com.chy.summer.framework.aop.TargetClassAware;
import com.chy.summer.framework.core.BridgeMethodResolver;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.ReflectionUtils;
import com.sun.istack.internal.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

/**
 * aop工具类
 */
public abstract class AopUtils {
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
}