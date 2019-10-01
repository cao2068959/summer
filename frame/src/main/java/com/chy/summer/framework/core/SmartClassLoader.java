package com.chy.summer.framework.core;

/**
 * 可重新加载的ClassLoader实现的接口
 * 如果ClassLoader没有实现此接口，则从该接口获得的所有类都应被视为不可重载
 */
public interface SmartClassLoader {

	/**
	 * 判断给定的类是否可重载
	 * 通常用于检查结果是否可以缓存或是否应每次重新获取
	 */
	boolean isClassReloadable(Class<?> clazz);

}