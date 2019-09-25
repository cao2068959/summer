package com.chy.summer.framework.core;

import com.chy.summer.framework.util.Assert;

/**
 * ThreadLocal的子类，额外拥有一个描述名称
 */
public class NamedThreadLocal<T> extends ThreadLocal<T> {

	private final String name;


	/**
	 * 使用给定名称创建一个新的NamedThreadLocal
	 * @param name 描述这个ThreadLocal的名称
	 */
	public NamedThreadLocal(String name) {
		Assert.hasText(name, "name不可为空");
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}

}