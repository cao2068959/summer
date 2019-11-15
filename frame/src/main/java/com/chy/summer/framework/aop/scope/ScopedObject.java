package com.chy.summer.framework.aop.scope;

import com.chy.summer.framework.aop.RawTargetAccess;

/**
 * 用于范围对象的AOP引入接口
 */
public interface ScopedObject extends RawTargetAccess {

	/**
	 * 以原始格式（存储在目标作用域中）返回此作用域对象代理后面的当前目标对象
	 * 例如，原始目标对象可以传递给持久性提供者，而持久性提供者将无法处理作用域的代理对象。
	 */
	Object getTargetObject();

	/**
	 * 将此对象从其目标范围中移除，例如从支持会话中移除
	 */
	void removeFromScope();

}