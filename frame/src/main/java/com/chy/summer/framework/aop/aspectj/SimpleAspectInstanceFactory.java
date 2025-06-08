package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.aop.framework.AopConfigException;
import com.chy.summer.framework.core.ordered.Ordered;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ReflectionUtils;
import javax.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;

/**
 * AspectInstanceFactory的实现，为每个getAspectInstance（）调用创建一个指定的切面类的新实例。
 */
public class SimpleAspectInstanceFactory implements AspectInstanceFactory {

	/**
	 * 切面类型
	 */
	private final Class<?> aspectClass;


	/**
	 * 为给定的切面类创建一个新的SimpleAspectInstanceFactory。
	 * @param aspectClass the aspect class
	 */
	public SimpleAspectInstanceFactory(Class<?> aspectClass) {
		Assert.notNull(aspectClass, "Aspectclass不可为空");
		this.aspectClass = aspectClass;
	}

	/**
	 * 获取切面类型
	 */
	public final Class<?> getAspectClass() {
		return this.aspectClass;
	}


	@Override
	public final Object getAspectInstance() {
		try {
			return ReflectionUtils.accessibleConstructor(this.aspectClass).newInstance();
		}
		catch (NoSuchMethodException ex) {
			throw new AopConfigException(
					"切面类上没有默认构造函数: " + this.aspectClass.getName(), ex);
		}
		catch (InstantiationException ex) {
			throw new AopConfigException(
					"无法实例化切面类: " + this.aspectClass.getName(), ex);
		}
		catch (IllegalAccessException ex) {
			throw new AopConfigException(
					"无法访问切面构造方法: " + this.aspectClass.getName(), ex);
		}
		catch (InvocationTargetException ex) {
			throw new AopConfigException(
					"无法调用切面构造器: " + this.aspectClass.getName(), ex.getTargetException());
		}
	}

	/**
	 * 获取类加载器
	 */
	@Override
	@Nullable
	public ClassLoader getAspectClassLoader() {
		return this.aspectClass.getClassLoader();
	}

	/**
	 * 获得该工厂的切面实例的顺序
	 */
	@Override
	public int getOrder() {
		return getOrderForAspectClass(this.aspectClass);
	}

	/**
	 * 当没有指定顺序的时候，调用此方法作为后备顺序
	 */
	protected int getOrderForAspectClass(Class<?> aspectClass) {
		return Ordered.LOWEST_PRECEDENCE;
	}

}