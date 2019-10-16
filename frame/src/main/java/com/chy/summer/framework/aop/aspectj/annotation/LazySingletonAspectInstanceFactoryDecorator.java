package com.chy.summer.framework.aop.aspectj.annotation;

import com.chy.summer.framework.util.Assert;
import com.sun.istack.internal.Nullable;

import java.io.Serializable;

/**
 * 延迟的单例切面实例化工厂装饰器
 */
public class LazySingletonAspectInstanceFactoryDecorator implements MetadataAwareAspectInstanceFactory, Serializable {

	private final MetadataAwareAspectInstanceFactory maaif;

	@Nullable
	private volatile Object materialized;


	/**
	 * 为给定的AspectInstanceFactory创建一个新的延迟初始化装饰器
	 * @param maaif the MetadataAwareAspectInstanceFactory to decorate
	 */
	public LazySingletonAspectInstanceFactoryDecorator(MetadataAwareAspectInstanceFactory maaif) {
		Assert.notNull(maaif, "AspectInstanceFactory不可为空");
		this.maaif = maaif;
	}

	/**
	 * 创建此工厂切面的实例
	 */
	@Override
	public Object getAspectInstance() {
		Object aspectInstance = this.materialized;
		if (aspectInstance == null) {
			//获取锁
			Object mutex = this.maaif.getAspectCreationMutex();
			if (mutex == null) {
				//单例对象不用锁
				aspectInstance = this.maaif.getAspectInstance();
				this.materialized = aspectInstance;
			}
			else {
				synchronized (mutex) {
					//原型模式下，最后一次获取实例对象，如果还是没有就进行新建
					aspectInstance = this.materialized;
					if (aspectInstance == null) {
						//通过beanFactory.getBean来获取或者创建原型对象
						aspectInstance = this.maaif.getAspectInstance();
						this.materialized = aspectInstance;
					}
				}
			}
		}
		return aspectInstance;
	}

	public boolean isMaterialized() {
		return (this.materialized != null);
	}

	/**
	 * 暴露此工厂使用的切面类加载器
	 */
	@Override
	@Nullable
	public ClassLoader getAspectClassLoader() {
		return this.maaif.getAspectClassLoader();
	}

	/**
	 * 获取这个工厂的aspect的AspectJ AspectMetadata。
	 * @return aspect的元数据
	 */
	@Override
	public AspectMetadata getAspectMetadata() {
		return this.maaif.getAspectMetadata();
	}

	/**
	 * 获取此工厂的最佳互斥对象
	 */
	@Override
	@Nullable
	public Object getAspectCreationMutex() {
		return this.maaif.getAspectCreationMutex();
	}

	/**
	 * 获取此对象的顺序值
	 * 值越小优先度越高，相同的顺序值将导致受影响对象的任意排序位置
	 */
	@Override
	public int getOrder() {
		return this.maaif.getOrder();
	}


	@Override
	public String toString() {
		return "LazySingletonAspectInstanceFactoryDecorator: 装饰了 " + this.maaif;
	}

}