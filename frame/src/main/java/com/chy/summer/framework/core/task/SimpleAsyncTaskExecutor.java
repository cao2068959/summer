package com.chy.summer.framework.core.task;

import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.core.ConcurrencyThrottleSupport;
import com.chy.summer.framework.util.core.CustomizableThreadCreator;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.sun.istack.internal.Nullable;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;

/**
 * TaskExecutor实现为每个任务启动一个新线程，以异步方式执行它
 *
 * 通过“concurrencyLimit”属性支持限制并发线程。 默认情况下，并发线程数是无限的
 * 注意：此实现不会重用线程！可以考虑使用线程池TaskExecutor实现，特别是对于执行大量短期任务
 */
public class SimpleAsyncTaskExecutor extends CustomizableThreadCreator
		implements AsyncListenableTaskExecutor, Serializable {

	/**
	 * 允许任意数量的并发调用
	 */
	public static final int UNBOUNDED_CONCURRENCY = ConcurrencyThrottleSupport.UNBOUNDED_CONCURRENCY;

	/**
	 * 不允许任何并发调用
	 */
	public static final int NO_CONCURRENCY = ConcurrencyThrottleSupport.NO_CONCURRENCY;


	/** 该执行程序使用的内部并发限制 */
	private final ConcurrencyThrottleAdapter concurrencyThrottle = new ConcurrencyThrottleAdapter();

	@Nullable
	private ThreadFactory threadFactory;

	@Nullable
	private TaskDecorator taskDecorator;


	/**
	 * 创建一个新的带有默认线程名称前缀的SimpleAsyncTaskExecutor
	 */
	public SimpleAsyncTaskExecutor() {
		super();
	}

	/**
	 * 使用给定的线程名称前缀创建一个新的SimpleAsyncTaskExecutor
	 * @param threadNamePrefix 用于新创建线程名称的前缀
	 */
	public SimpleAsyncTaskExecutor(String threadNamePrefix) {
		super(threadNamePrefix);
	}

	/**
	 * 使用给定的外部线程工厂创建一个新的SimpleAsyncTaskExecutor
	 * @param threadFactory 用于创建新线程的工厂
	 */
	public SimpleAsyncTaskExecutor(ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
	}


	/**
	 * 指定用于创建新线程的外部工厂，而不是依赖于此执行程序的本地属性
	 */
	public void setThreadFactory(@Nullable ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
	}

	/**
	 * 返回用于创建新线程的工厂
	 */
	@Nullable
	public final ThreadFactory getThreadFactory() {
		return this.threadFactory;
	}

	/**
	 * 指定一个自定义TaskDecorator应用于要执行的任何Runnable
	 * 请注意，这样的装饰器不一定适用于用户提供的Runnable / Callable，而是适用于实际的执行回调
	 */
	public final void setTaskDecorator(TaskDecorator taskDecorator) {
		this.taskDecorator = taskDecorator;
	}

	/**
	 * 设置允许的最大并行访问数
	 * -1表示完全没有并发限制
	 * 此限制可以在运行时更改，尽管通常将其设计为配置时间设置
	 * 但是不要在运行时在-1和任何具体的限制之间切换，因为这可能会导致并发数不一致
	 */
	public void setConcurrencyLimit(int concurrencyLimit) {
		this.concurrencyThrottle.setConcurrencyLimit(concurrencyLimit);
	}

	/**
	 * 获取允许的最大并行访问数
	 */
	public final int getConcurrencyLimit() {
		return this.concurrencyThrottle.getConcurrencyLimit();
	}

	/**
	 * 获取该并发限制当前是否处于活动状态
	 */
	public final boolean isThrottleActive() {
		return this.concurrencyThrottle.isThrottleActive();
	}


	/**
	 * 执行给定的任务
	 */
	@Override
	public void execute(Runnable task) {
		execute(task, TIMEOUT_INDEFINITE);
	}

	/**
	 * 执行给定的任务
	 */
	@Override
	public void execute(Runnable task, long startTimeout) {
		Assert.notNull(task, "Runnable不能为空");
		//判断是否需要装饰任务
		Runnable taskToUse = (this.taskDecorator != null ? this.taskDecorator.decorate(task) : task);
		if (isThrottleActive() && startTimeout > TIMEOUT_IMMEDIATE) {
			this.concurrencyThrottle.beforeAccess();
			doExecute(new ConcurrencyThrottlingRunnable(taskToUse));
		}
		else {
			//不带任务时间执行
			doExecute(taskToUse);
		}
	}

	/**
	 * 提交要执行的Runnable任务，并接收代表该任务的Future
	 */
	@Override
	public Future<?> submit(Runnable task) {
		FutureTask<Object> future = new FutureTask<>(task, null);
		execute(future, TIMEOUT_INDEFINITE);
		return future;
	}

	/**
	 * 提交要执行的Callable任务，接收代表该任务的Future
	 */
	@Override
	public <T> Future<T> submit(Callable<T> task) {
		FutureTask<T> future = new FutureTask<>(task);
		execute(future, TIMEOUT_INDEFINITE);
		return future;
	}

	/**
	 * 提交要执行的Runnable任务，接收代表该任务的ListenableFuture
	 */
	@Override
	public ListenableFuture<?> submitListenable(Runnable task) {
		ListenableFutureTask<Object> future = ListenableFutureTask.create(task,null);
		execute(future, TIMEOUT_INDEFINITE);
		return future;
	}

	/**
	 * 提交要执行的Callable任务，接收代表该任务的ListenableFuture
	 */
	@Override
	public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
		ListenableFutureTask<T> future = ListenableFutureTask.create(task);
		execute(future, TIMEOUT_INDEFINITE);
		return future;
	}

	/**
	 * 实际执行任务的模板方法
	 * 默认实现创建一个新线程并启动它
	 * @param task 需要执行的任务
	 */
	protected void doExecute(Runnable task) {
		Thread thread = (this.threadFactory != null ? this.threadFactory.newThread(task) : createThread(task));
		thread.start();
	}

	/**
	 * 通用ConcurrencyThrottleSupport类的子类，使beforeAccess()和afterAccess()对周围的类可见
	 */
	private static class ConcurrencyThrottleAdapter extends ConcurrencyThrottleSupport {

		@Override
		protected void beforeAccess() {
			super.beforeAccess();
		}

		@Override
		protected void afterAccess() {
			super.afterAccess();
		}
	}


	/**
	 * 这个Runnable在目标Runnable完成执行后调用afterAccess()
	 */
	private class ConcurrencyThrottlingRunnable implements Runnable {

		private final Runnable target;

		public ConcurrencyThrottlingRunnable(Runnable target) {
			this.target = target;
		}

		@Override
		public void run() {
			try {
				this.target.run();
			}
			finally {
				concurrencyThrottle.afterAccess();
			}
		}
	}

}
