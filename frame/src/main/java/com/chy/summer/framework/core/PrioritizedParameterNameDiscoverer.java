package com.chy.summer.framework.core;

import javax.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * 依次尝试多个参数名实现器。 在addDiscoverer()方法中最先添加的那些优先级最高。 如果一个返回null，将尝试下一个。
 * 如果没有发现者匹配，则默认行为是返回null。
 */
public class PrioritizedParameterNameDiscoverer implements ParameterNameDiscoverer {

	/**
	 * 参数名发现器，越靠前的优先级越高
	 */
	private final List<ParameterNameDiscoverer> parameterNameDiscoverers = new LinkedList<>();


	/**
	 * 将一个新的参数名实现器添加到此PrioritizedParameterNameDiscoverer中。
	 */
	public void addDiscoverer(ParameterNameDiscoverer pnd) {
		this.parameterNameDiscoverers.add(pnd);
	}

	/**
	 * 获取方法的参数
	 */
	@Override
	@Nullable
	public String[] getParameterNames(Method method) {
		//逐一使用参数名发现器，对方法参数获取
		for (ParameterNameDiscoverer pnd : this.parameterNameDiscoverers) {
			//获取参数
			String[] result = pnd.getParameterNames(method);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * 获取构造方法的参数
	 */
	@Override
	@Nullable
	public String[] getParameterNames(Constructor<?> ctor) {
		for (ParameterNameDiscoverer pnd : this.parameterNameDiscoverers) {
			String[] result = pnd.getParameterNames(ctor);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

}