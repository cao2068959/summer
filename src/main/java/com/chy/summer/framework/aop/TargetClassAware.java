package com.chy.summer.framework.aop;

import com.sun.istack.internal.Nullable;

/**
 * 用于将目标类暴露在代理之后的最小接口。
 * 由AOP代理对象和代理工厂(通过advise)以及目标源实现。
 */
public interface TargetClassAware {

	/**
	 * 返回实例对象后的目标类
	 * 这个方法用于获取对象的真实类型
	 */
	@Nullable
	Class<?> getTargetClass();
}