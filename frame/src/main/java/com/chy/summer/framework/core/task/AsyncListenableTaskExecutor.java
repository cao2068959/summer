package com.chy.summer.framework.core.task;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Callable;

/**
 * 扩展了AsyncTaskExecutor接口，增加为ListenableFuture提交Task的功能
 */
public interface AsyncListenableTaskExecutor extends AsyncTaskExecutor {

	/**
	 * 提交要执行的Runnable任务，接收代表该任务的ListenableFuture
     * Future将在完成后返回空结果。
	 * @param task 要执行的Runnable（不可为null）
	 */
	ListenableFuture<?> submitListenable(Runnable task);

	/**
     * 提交要执行的Callable任务，接收代表该任务的ListenableFuture
     * 在以后完成时返回可调用的结果
	 * @param task 要执行的Callable（不可为null）
	 */
	<T> ListenableFuture<T> submitListenable(Callable<T> task);

}