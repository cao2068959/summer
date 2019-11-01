package com.chy.summer.framework.core.task;

import java.util.concurrent.Executor;

@FunctionalInterface
public interface TaskExecutor extends Executor {

	/**
	 * 执行给定的任务
	 * 如果实现使用异步执行策略，则该调用可能立即返回，或者在同步执行的情况下，调用可能会阻塞。
	 * @param task 要执行的Runnable（不可为null）
	 */
	@Override
	void execute(Runnable task);
}