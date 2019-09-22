package com.chy.summer.framework.aop.aopalliance.intercept;

/**
 * aop程序中的调用根接口
 */
public interface Invocation extends Joinpoint {

	/**
	 * 获取连接点的方法参数，根据顺序转为数组
	 */
	Object[] getArguments();

}