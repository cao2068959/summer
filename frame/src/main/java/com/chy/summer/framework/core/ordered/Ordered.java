package com.chy.summer.framework.core.ordered;

/**
 * Ordered是一个接口，可以由应该是可排序的对象来实现。
 * 实际order可以解释为优先级排序，第一个对象（具有最低顺序值,值越小优先度越高）具有最高优先级。
 */
public interface Ordered {

	/**
	 * 最高优先级值的有用常数
	 */
	int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

	/**
	 * 最低优先级值的有用常数
	 */
	int LOWEST_PRECEDENCE = Integer.MAX_VALUE;


	/**
	 * 获取此对象的顺序值
	 * 值越小优先度越高，相同的顺序值将导致受影响对象的任意排序位置
	 */
	int getOrder();
}