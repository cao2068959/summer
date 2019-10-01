package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.aop.SummerProxy;
import com.chy.summer.framework.aop.TargetClassAware;
import com.chy.summer.framework.aop.TargetSource;
import com.chy.summer.framework.aop.support.AopUtils;
import com.chy.summer.framework.aop.target.SingletonTargetSource;
import com.chy.summer.framework.core.DecoratingProxy;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ObjectUtils;
import com.sun.istack.internal.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * AOP代理工厂的工具类
 */
public abstract class AopProxyUtils {

	/**
	 * 获取给定代理里的单例目标对象
	 */
	@Nullable
	public static Object getSingletonTarget(Object candidate) {
		if (candidate instanceof Advised) {
			//获取目标源
			TargetSource targetSource = ((Advised) candidate).getTargetSource();
			if (targetSource instanceof SingletonTargetSource) {
				//如果是个SingletonTargetSource再获取持有的Target
				return ((SingletonTargetSource) targetSource).getTarget();
			}
		}
		return null;
	}

	/**
	 * 获取给定bean实例的最终目标类，不仅遍历顶级代理，而且遍历任何数量的嵌套代理。
	 * @param candidate 要检查的实例（可能是AOP代理
	 */
	public static Class<?> ultimateTargetClass(Object candidate) {
		Assert.notNull(candidate, "指定对象不能为空");
		Object current = candidate;
		Class<?> result = null;
		while (current instanceof TargetClassAware) {
			//获取目标的类型
			result = ((TargetClassAware) current).getTargetClass();
			//获取给定代理里的单例目标对象
			current = getSingletonTarget(current);
		}
		if (result == null) {
			//如果没有获取到目标类型,判断是不是cglib代理，不管是不是都要获取到原对象的类型
			result = (AopUtils.isCglibProxy(candidate) ? candidate.getClass().getSuperclass() : candidate.getClass());
		}
		return result;
	}

	/**
	 * 获取给定AOP配置的代理的完整接口列表。
	 */
	public static Class<?>[] completeProxiedInterfaces(AdvisedSupport advised) {
		return completeProxiedInterfaces(advised, false);
	}

	/**
	 * 获取给定AOP配置的代理的完整接口列表。
	 * @param decoratingProxy 是否公开DecoratingProxy接口
	 */
	static Class<?>[] completeProxiedInterfaces(AdvisedSupport advised, boolean decoratingProxy) {
		//获取aop代理的接口
		Class<?>[] specifiedInterfaces = advised.getProxiedInterfaces();
		if (specifiedInterfaces.length == 0) {
			//没有用户指定的接口，检查目标类是否是接口。
			Class<?> targetClass = advised.getTargetClass();
			if (targetClass != null) {
				//获取目标源判断是否是接口
				if (targetClass.isInterface()) {
					//如果是接口将其设置到advised中
					advised.setInterfaces(targetClass);
				}
				else if (Proxy.isProxyClass(targetClass)) {
					//如果是代理类，就将其代理的接口设置到advised中
					advised.setInterfaces(targetClass.getInterfaces());
				}
				//再次获取aop代理的接口
				specifiedInterfaces = advised.getProxiedInterfaces();
			}
		}
		//没有代理SummerProxy
		boolean addSummerProxy = !advised.isInterfaceProxied(SummerProxy.class);
		//不可以转为查询代理状态,并且没有代理advised
		boolean addAdvised = !advised.isOpaque() && !advised.isInterfaceProxied(Advised.class);
		//公开decoratingProxy并且没有代理DecoratingProxy
		boolean addDecoratingProxy = (decoratingProxy && !advised.isInterfaceProxied(DecoratingProxy.class));
		//非用户指定的接口数量
		int nonUserIfcCount = 0;
		if (addSummerProxy) {
			nonUserIfcCount++;
		}
		if (addAdvised) {
			nonUserIfcCount++;
		}
		if (addDecoratingProxy) {
			nonUserIfcCount++;
		}
		Class<?>[] proxiedInterfaces = new Class<?>[specifiedInterfaces.length + nonUserIfcCount];
		//将specifiedInterfaces里的元素拷贝到proxiedInterfaces中
		System.arraycopy(specifiedInterfaces, 0, proxiedInterfaces, 0, specifiedInterfaces.length);
		int index = specifiedInterfaces.length;
		//应为用户没有实现直接接口，但是又是必须的，我们自己给他填上这些接口
		if (addSummerProxy) {
			proxiedInterfaces[index] = SummerProxy.class;
			index++;
		}
		if (addAdvised) {
			proxiedInterfaces[index] = Advised.class;
			index++;
		}
		if (addDecoratingProxy) {
			proxiedInterfaces[index] = DecoratingProxy.class;
		}
		return proxiedInterfaces;
	}

	/**
	 * 获取给定代理实现的用户指定接口，即代理实现的所有非advice接口。
	 * 与上面的接口相反，之前是增加，现在是删除掉这些接口
	 */
	public static Class<?>[] proxiedUserInterfaces(Object proxy) {
		Class<?>[] proxyInterfaces = proxy.getClass().getInterfaces();
		//非用户指定的接口数量
		int nonUserIfcCount = 0;
		if (proxy instanceof SummerProxy) {
			nonUserIfcCount++;
		}
		if (proxy instanceof Advised) {
			nonUserIfcCount++;
		}
		if (proxy instanceof DecoratingProxy) {
			nonUserIfcCount++;
		}
		Class<?>[] userInterfaces = new Class<?>[proxyInterfaces.length - nonUserIfcCount];
		//复制proxyInterfaces里的元素到userInterfaces中，根据上面求出的非用户实现的接口数量，最后几个不复制
		System.arraycopy(proxyInterfaces, 0, userInterfaces, 0, userInterfaces.length);
		Assert.notEmpty(userInterfaces, "JDK代理必须要有一个或者多个接口");
		return userInterfaces;
	}

	/**
	 * 检查给定AdvisedSupport对象里的代理是否相等。
	 */
	public static boolean equalsInProxy(AdvisedSupport a, AdvisedSupport b) {
		//a和b是用一个对象，或者接口，advisor和目标源的值相等
		return (a == b ||
				(equalsProxiedInterfaces(a, b) && equalsAdvisors(a, b) && a.getTargetSource().equals(b.getTargetSource())));
	}

	/**
	 * 检查给定AdvisedSupport对象里的代理是否相等
	 */
	public static boolean equalsProxiedInterfaces(AdvisedSupport a, AdvisedSupport b) {
		return Arrays.equals(a.getProxiedInterfaces(), b.getProxiedInterfaces());
	}

	/**
	 * 检查给定AdvisedSupport对象里的advisor是否相等。
	 */
	public static boolean equalsAdvisors(AdvisedSupport a, AdvisedSupport b) {
		return Arrays.equals(a.getAdvisors(), b.getAdvisors());
	}


	/**
	 * 给定vararg参数数组与方法中声明的vararg参数的数组类型不匹配时，修改参数的类型
	 */
	static Object[] adaptArgumentsIfNecessary(Method method, @Nullable Object[] arguments) {
		if (ObjectUtils.isEmpty(arguments)) {
			return new Object[0];
		}
		//判断这个方法是否采用了可变数量的参数
		if (method.isVarArgs()) {
			//获取参数类型
			Class<?>[] paramTypes = method.getParameterTypes();
			if (paramTypes.length == arguments.length) {
				//可变参数的索引
				int varargIndex = paramTypes.length - 1;
				//获取可变参数的索引
				Class<?> varargType = paramTypes[varargIndex];
				if (varargType.isArray()) {
					Object varargArray = arguments[varargIndex];
					//如果varargArray是Object数组，并且varargArray不是varargType的实例对象
					if (varargArray instanceof Object[] && !varargType.isInstance(varargArray)) {
						//创建新的参数
						Object[] newArguments = new Object[arguments.length];
						System.arraycopy(arguments, 0, newArguments, 0, varargIndex);
						//获取组件的类型
						Class<?> targetElementType = varargType.getComponentType();
						//获取元素的数量
						int varargLength = Array.getLength(varargArray);
						//创建新的对象，Array.newInstance返回的实际上是Object[]
						Object newVarargArray = Array.newInstance(targetElementType, varargLength);
						//将参数复制到新的对象中
						System.arraycopy(varargArray, 0, newVarargArray, 0, varargLength);
						newArguments[varargIndex] = newVarargArray;
						//返回新的参数
						return newArguments;
					}
				}
			}
		}
		return arguments;
	}

}