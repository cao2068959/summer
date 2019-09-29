package com.chy.summer.framework.aop.target;

import com.chy.summer.framework.aop.TargetSource;
import com.chy.summer.framework.util.ObjectUtils;

import java.io.Serializable;

/**
 * TargetSource在没有目标,并且行为由接口和advisor提供的情况下的规范。
 */
public class EmptyTargetSource implements TargetSource, Serializable {

	/**
	 * 此规范的实例
	 */
	public static final EmptyTargetSource INSTANCE = new EmptyTargetSource(null, true);


	/**
	 * 返回给定目标类的EmptyTargetSource
	 */
	public static EmptyTargetSource forClass(Class<?> targetClass) {
		return forClass(targetClass, true);
	}

	/**
	 * 返回给定目标类的EmptyTargetSource
	 */
	public static EmptyTargetSource forClass(Class<?> targetClass, boolean isStatic) {
		return (targetClass == null && isStatic ? INSTANCE : new EmptyTargetSource(targetClass, isStatic));
	}


	private final Class<?> targetClass;

	private final boolean isStatic;


	/**
	 * 创建EmptyTargetSource类的新实例。
	 * 此构造函数是private，用于Singleton模式/工厂方法模式。
	 */
	private EmptyTargetSource(Class<?> targetClass, boolean isStatic) {
		this.targetClass = targetClass;
		this.isStatic = isStatic;
	}

	/**
	 * 始终返回指定的目标类，如果没有返回null
	 */
	@Override
	public Class<?> getTargetClass() {
		return this.targetClass;
	}

	/**
	 * 是否是静态的，总是返回true
	 */
	@Override
	public boolean isStatic() {
		return this.isStatic;
	}

	/**
	 * 获取目标，总是返回null
	 */
	@Override
	public Object getTarget() {
		return null;
	}

	@Override
	public void releaseTarget(Object target) {
	}


	/**
	 * 如果没有目标类，则返回反序列化的规范实例，从而保护Singleton模式。
	 */
	private Object readResolve() {
		return (this.targetClass == null && this.isStatic ? INSTANCE : this);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof EmptyTargetSource)) {
			return false;
		}
		EmptyTargetSource otherTs = (EmptyTargetSource) other;
		return (ObjectUtils.nullSafeEquals(this.targetClass, otherTs.targetClass) && this.isStatic == otherTs.isStatic);
	}

	@Override
	public int hashCode() {
		return EmptyTargetSource.class.hashCode() * 13 + ObjectUtils.nullSafeHashCode(this.targetClass);
	}

	@Override
	public String toString() {
		return "EmptyTargetSource: " +
				(this.targetClass != null ? "target class [" + this.targetClass.getName() + "]" : "no target class") +
				", " + (this.isStatic ? "static" : "dynamic");
	}

}