package com.chy.summer.framework.aop.support;

import com.chy.summer.framework.aop.IntroductionInfo;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.chy.summer.framework.util.ClassUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IntroductionInfo的实现。
 * 允许目标方法的子类方便地添加给定对象的所有接口，并禁止不应添加的接口。
 * 还允许查询所有引入的接口。
 */
public class IntroductionInfoSupport implements IntroductionInfo, Serializable {

	/**
	 * 公开的接口
	 */
	protected final Set<Class<?>> publishedInterfaces = new LinkedHashSet<>();

	/**
	 * 公开接口下是否包含了此方法的缓存
	 * 方法与包含关系的映射
	 */
	private transient Map<Method, Boolean> rememberedMethods = new ConcurrentHashMap<>(32);


	/**
	 * 去除指定的接口，
	 * 如果委托实现了该接口，该接口可能已被自动检测到。
	 * 调用此方法使内部接口在代理级别不可见。
	 * 如果接口未由委托实现，则不执行任何操作。
	 */
	public void suppressInterface(Class<?> intf) {
		this.publishedInterfaces.remove(intf);
	}

	/**
	 * 返回Advisor或者Advice引入的其他接口。
	 */
	@Override
	public Class<?>[] getInterfaces() {
		return this.publishedInterfaces.toArray(new Class<?>[this.publishedInterfaces.size()]);
	}

	/**
	 * 检查指定的接口是否为已公开的接口,或者是其父类
	 */
	public boolean implementsInterface(Class<?> ifc) {
		for (Class<?> pubIfc : this.publishedInterfaces) {
			if (ifc.isInterface() && ifc.isAssignableFrom(pubIfc)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 公开给定委托在代理级别实现的所有接口。
	 */
	protected void implementInterfacesOnObject(Object delegate) {
		this.publishedInterfaces.addAll(ClassUtils.getAllInterfacesAsSet(delegate));
	}

	/**
	 * 判断指定方法是否在引入的接口上
	 */
	protected final boolean isMethodOnIntroducedInterface(MethodInvocation mi) {
		//方法的缓存
		Boolean rememberedResult = this.rememberedMethods.get(mi.getMethod());
		if (rememberedResult != null) {
			return rememberedResult;
		}
		else {
			//查询这个方法的所在类的类型是否在已公开的接口中
			boolean result = implementsInterface(mi.getMethod().getDeclaringClass());
			//缓存查询结果
			this.rememberedMethods.put(mi.getMethod(), result);
			return result;
		}
	}


	/**
	 * 对序列化的支持
	 */
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		this.rememberedMethods = new ConcurrentHashMap<>(32);
	}

}
