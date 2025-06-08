package com.chy.summer.framework.core;

import javax.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * 用于发现方法和构造函数的参数名称的接口。
 * 不总是可以发现参数名称，但是可以尝试各种策略，
 * 例如寻找可能在编译时发出的调试信息，以及寻找可选地带有AspectJ注释方法的argname注释值。
 */
public interface ParameterNameDiscoverer {

	/**
	 * 返回方法的参数名称；如果无法确定，则返回null。
	 * 如果只能获取到部分的参数名，则数组中的个别元素可能为null。
	 */
	@Nullable
	String[] getParameterNames(Method method);

	/**
	 * 返回此构造函数的参数名称；如果无法确定，则返回null。
	 */
	@Nullable
	String[] getParameterNames(Constructor<?> ctor);

}