package com.chy.summer.framework.aop.framework;

import com.chy.summer.framework.core.NamedThreadLocal;
import com.sun.istack.internal.Nullable;

/**
 * 包含用于获取有关当前AOP调用信息的静态方法的类。
 *
 * 如果将AOP框架配置为公开当前代理，则currentProxy方法可用
 * 它返回正在使用的AOP代理。目标对象或advice可以使用它来进行advice的调用,同时可以查找建议配置。
 * AOP框架默认情况下不公开代理，因为这样做会降低性能。
 */
public abstract class AopContext {

	/**
	 * 当前的本地线程变量
	 */
	private static final ThreadLocal<Object> currentProxy = new NamedThreadLocal<>("当前的AOP代理");


	/**
	 * 尝试返回当前的AOP代理
	 * 只有通过AOP调用了调用方法后，并且已将AOP框架设置为公开代理时，此方法才可用
	 */
	public static Object currentProxy() throws IllegalStateException {
		Object proxy = currentProxy.get();
		if (proxy == null) {
			throw new IllegalStateException(
					"找不到当前代理：在“advice”的“ exposeProxy”属性设置为“ true”才可以使用。");
		}
		return proxy;
	}

	/**
	 * 在本地线程中设置代理对象，并返回原先在本地线程中的代理对象
	 * 调用方应注意保留这个酒的代理对象
	 */
	@Nullable
	static Object setCurrentProxy(@Nullable Object proxy) {
		Object old = currentProxy.get();
		if (proxy != null) {
			currentProxy.set(proxy);
		}
		else {
			currentProxy.remove();
		}
		return old;
	}

}