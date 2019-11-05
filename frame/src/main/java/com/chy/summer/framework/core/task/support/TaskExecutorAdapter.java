package com.chy.summer.framework.core.task.support;

import com.chy.summer.framework.core.task.AsyncListenableTaskExecutor;
import com.chy.summer.framework.core.task.TaskDecorator;
import com.chy.summer.framework.core.task.TaskRejectedException;
import com.chy.summer.framework.util.Assert;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.*;

/**
 * 接受JDK执行器并为其公开任务执行器的适配器。还可以检测扩展的ExecutorService，相应地调整AsyncTaskExecutor接口
 */
public class TaskExecutorAdapter implements AsyncListenableTaskExecutor {

	private final Executor concurrentExecutor;

	@Nullable
	private TaskDecorator taskDecorator;


	/**
	 * 使用给定的JDK并发执行器创建一个新的TaskExecutorAdapter
	 * @param concurrentExecutor 委托给的JDK并发执行器
	 */
	public TaskExecutorAdapter(Executor concurrentExecutor) {
		Assert.notNull(concurrentExecutor, "Executor不可为空");
		this.concurrentExecutor = concurrentExecutor;
	}


	/**
	 * 指定一个自定义TaskDecorator应用于将要执行的Runnable
	 * 注意，这样的装饰器不一定应用于用户提供的Runnable/Callable，而是应用于实际的执行回调(可能是用户提供任务的装饰器)，即已经装饰过一次或者多次的装饰器
	 * 主要用于围绕任务调用设置一些执行上下文，或者为任务执行提供一些监视/统计信息
	 */
	public final void setTaskDecorator(TaskDecorator taskDecorator) {
		this.taskDecorator = taskDecorator;
	}

	/**
	 * 委托给指定的JDK并发执行器
	 */
	@Override
	public void execute(Runnable task) {
		try {
			doExecute(this.concurrentExecutor, this.taskDecorator, task);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(
					"执行者 [" + this.concurrentExecutor + "] 不接受任务：" + task, ex);
		}
	}

	/**
	 * 获取执行任务.
	 */
	@Override
	public void execute(Runnable task, long startTimeout) {
		execute(task);
	}

	/**
	 * 提交要执行的Runnable任务，并接收代表该任务的Future
	 * Future将在完成后返回空结果
	 * @param task 要执行的Runnable（不可为null）
	 */
	@Override
	public Future<?> submit(Runnable task) {
		try {
			if (this.taskDecorator == null && this.concurrentExecutor instanceof ExecutorService) {
				return ((ExecutorService) this.concurrentExecutor).submit(task);
			}
			else {
				FutureTask<Object> future = new FutureTask<>(task, null);
				doExecute(this.concurrentExecutor, this.taskDecorator, future);
				return future;
			}
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(
					"执行者 [" + this.concurrentExecutor + "] 不接受任务：" + task, ex);
		}
	}

	/**
	 * 提交要执行的Callable任务，接收代表该任务的Future
	 * 在以后完成时返回可调用的结果
	 * @param task 要执行的Callable（不可为null）
	 */
	@Override
	public <T> Future<T> submit(Callable<T> task) {
		try {
			if (this.taskDecorator == null && this.concurrentExecutor instanceof ExecutorService) {
				return ((ExecutorService) this.concurrentExecutor).submit(task);
			}
			else {
				FutureTask<T> future = new FutureTask<>(task);
				doExecute(this.concurrentExecutor, this.taskDecorator, future);
				return future;
			}
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(
					"执行者 [" + this.concurrentExecutor + "] 不接受任务：" + task, ex);
		}
	}

	/**
	 * 提交要执行的Runnable任务，接收代表该任务的ListenableFuture
	 * Future将在完成后返回空结果。
	 * @param task 要执行的Runnable（不可为null）
	 */
	@Override
	public ListenableFuture<?> submitListenable(Runnable task) {
		try {
			ListenableFutureTask<Object> future = new ListenableFutureTask<>(task, null);
			doExecute(this.concurrentExecutor, this.taskDecorator, future);
			return future;
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(
					"执行者 [" + this.concurrentExecutor + "] 不接受任务：" + task, ex);
		}
	}

	/**
	 * 提交要执行的Callable任务，接收代表该任务的ListenableFuture
	 * 在以后完成时返回可调用的结果
	 * @param task 要执行的Callable（不可为null）
	 */
	@Override
	public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
		try {
			ListenableFutureTask<T> future = new ListenableFutureTask<>(task);
			doExecute(this.concurrentExecutor, this.taskDecorator, future);
			return future;
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(
					"执行者 [" + this.concurrentExecutor + "] 不接受任务：" + task, ex);
		}
	}


	/**
	 * 使用给定的执行器实际执行给定的Runnable
	 * @param concurrentExecutor 要委托给的底层JDK并发执行程序
	 * @param taskDecorator 指定要应用的装饰器(如果有的话)
	 * @param runnable 可运行的执行
	 */
	protected void doExecute(Executor concurrentExecutor, @Nullable TaskDecorator taskDecorator, Runnable runnable)
			throws RejectedExecutionException{

		concurrentExecutor.execute(taskDecorator != null ? taskDecorator.decorate(runnable) : runnable);
	}

}