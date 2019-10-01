package com.chy.summer.framework.core;

/**
 * 通过装饰代理实现的接口，但也可能具有装饰器语义的自定义代理
 */
public interface DecoratingProxy {

	/**
	 * 返回此代理后面的修饰类
	 * 对于AOP代理，会返回最终目标类，而不仅仅是在多个嵌套代理的情况下的直接目标
	 */
	Class<?> getDecoratedClass();

}