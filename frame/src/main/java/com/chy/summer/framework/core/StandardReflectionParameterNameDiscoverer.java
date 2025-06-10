package com.chy.summer.framework.core;

import javax.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * ParameterNameDiscoverer的实现，该实现使用JDK 8的反射功能来检查参数名称
 */
public class StandardReflectionParameterNameDiscoverer implements ParameterNameDiscoverer {

	/**
	 * 获取方法的参数
	 */
	@Override
	@Nullable
	public String[] getParameterNames(Method method) {
		return getParameterNames(method.getParameters());
	}

	/**
	 * 获取构造方法的参数
	 */
	@Override
	@Nullable
	public String[] getParameterNames(Constructor<?> ctor) {
		return getParameterNames(ctor.getParameters());
	}

	/**
	 * 通过参数列表获取参数名
	 * @param parameters
	 * @return
	 */
	@Nullable
	private String[] getParameterNames(Parameter[] parameters) {
		String[] parameterNames = new String[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			Parameter param = parameters[i];
			if (!param.isNamePresent()) {
				//名称不存在
				return null;
			}
			parameterNames[i] = param.getName();
		}
		return parameterNames;
	}

}