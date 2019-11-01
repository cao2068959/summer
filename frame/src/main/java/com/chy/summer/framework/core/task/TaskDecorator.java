package com.chy.summer.framework.core.task;

/**
 * 一个回调接口，用于将装饰器应用于将要执行的任何Runnable
 * 注意，这样的装饰器不一定应用于用户提供的Runnable/Callable，而是应用于实际的执行回调(可能是用户提供任务的装饰器)，即已经装饰过一次或者多次的装饰器
 */
@FunctionalInterface
public interface TaskDecorator {

	/**
	 * 修饰给定的Runnable，为实际执行返回一个可能包装的Runnable
	 * @param runnable 原始的Runnable
	 */
	Runnable decorate(Runnable runnable);

}