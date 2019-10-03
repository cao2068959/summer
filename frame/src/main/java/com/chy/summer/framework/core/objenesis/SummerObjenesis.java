package com.chy.summer.framework.core.objenesis;

import com.chy.summer.framework.core.SummerProperties;
import com.chy.summer.framework.util.ConcurrentReferenceHashMap;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisException;
import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.strategy.InstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 * 特定于summer的ObjenesisStd / ObjenesisBase变形，提供基于class -key而不是类名的缓存，并允许选择性使用缓存。
 */
public class SummerObjenesis implements Objenesis {

	/**
	 * 指示summer忽略Objenesis的系统属性，甚至不尝试使用它。
	 * 将此标志设置为“ true”等效于让summer找出Objenesis在运行时不起作用，立即触发后备代码路径：最重要的是，这意味着所有CGLIB AOP代理都将通过默认构造函数通过常规实例化来创建。 。
	 */
	public static final String IGNORE_OBJENESIS_PROPERTY_NAME = "summer.objenesis.ignore";


	private final InstantiatorStrategy strategy;

	private final ConcurrentReferenceHashMap<Class<?>, ObjectInstantiator<?>> cache =
			new ConcurrentReferenceHashMap<>();

	/**
	 * 是否值得尝试初始化
	 */
	private volatile Boolean worthTrying;


	/**
	 * 使用标准实例化策略创建一个新的SummerObjenesis实例
	 */
	public SummerObjenesis() {
		this(null);
	}

	/**
	 * 使用给定的标准实例化策略创建一个新的Summer Objenesis实例。
	 */
	public SummerObjenesis(InstantiatorStrategy strategy) {
		this.strategy = (strategy != null ? strategy : new StdInstantiatorStrategy());

		//预先评估“ summer.objenesis.ignore”属性...
		if (SummerProperties.getFlag(SummerObjenesis.IGNORE_OBJENESIS_PROPERTY_NAME)) {
			this.worthTrying = Boolean.FALSE;
		}
	}


	/**
	 * 返回此Objenesis实例是否值得尝试创建实例，即是否尚未使用或已知可以正常工作。
	 * 如果已识别出已配置的Objenesis实例化器策略根本无法在当前JVM上运行，
	 * 或者如果将“ summer.objenesis.ignore”属性设置为“ true”，则此方法返回false。
	 */
	public boolean isWorthTrying() {
		return (this.worthTrying != Boolean.FALSE);
	}

	/**
	 * 通过Objenesis创建给定类的新实例。
	 * @param clazz 创建该类一个实例
	 * @param useCache 是否使用实例化器缓存
	 */
	public <T> T newInstance(Class<T> clazz, boolean useCache) {
		if (!useCache) {
			//新建一个
			return newInstantiatorOf(clazz).newInstance();
		}
		return getInstantiatorOf(clazz).newInstance();
	}

	/**
	 * 实例化指定类
	 */
	public <T> T newInstance(Class<T> clazz) {
		return getInstantiatorOf(clazz).newInstance();
	}

	/**
	 * 从缓存中获取实例对象
	 */
	public <T> ObjectInstantiator<T> getInstantiatorOf(Class<T> clazz) {
		//从缓存中获取实例数据
		ObjectInstantiator<?> instantiator = this.cache.get(clazz);
		if (instantiator == null) {
			//没有获取到实例化对象
			ObjectInstantiator<T> newInstantiator = newInstantiatorOf(clazz);
			//如果缓存中不存在，则存入缓存
			instantiator = this.cache.putIfAbsent(clazz, newInstantiator);
			if (instantiator == null) {
				instantiator = newInstantiator;
			}
		}
		return (ObjectInstantiator<T>) instantiator;
	}

	/**
	 * 实例化指定类
	 */
	protected <T> ObjectInstantiator<T> newInstantiatorOf(Class<T> clazz) {
		//当前是否值得尝试创建
		Boolean currentWorthTrying = this.worthTrying;
		try {
			//使用实例化策略
			ObjectInstantiator<T> instantiator = this.strategy.newInstantiatorOf(clazz);
			if (currentWorthTrying == null) {
				//设置值得尝试
				this.worthTrying = Boolean.TRUE;
			}
			return instantiator;
		}
		catch (ObjenesisException ex) {
			if (currentWorthTrying == null) {
				Throwable cause = ex.getCause();
				if (cause instanceof ClassNotFoundException || cause instanceof IllegalAccessException) {
					//实例化失败，设置成不值得尝试
					this.worthTrying = Boolean.FALSE;
				}
			}
			throw ex;
		}
		catch (NoClassDefFoundError err) {
			if (currentWorthTrying == null) {
				this.worthTrying = Boolean.FALSE;
			}
			throw new ObjenesisException(err);
		}
	}

}