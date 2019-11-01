package com.chy.summer.framework.core.task;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 异步TaskExecutor实现的扩展接口，提供一个重载的execute(Runnable, long)变量，带有一个启动超时参数，并支持Callable
 * Executors类包含一组方法，可以将其他一些类似于关闭的对象(例如PrivilegedAction)转换为Callable，然后再执行它们
 * 实现这个接口还表明execute(Runnable)方法不会在调用器的线程中执行Runnable，而是在其他线程中异步执行
 */
public interface AsyncTaskExecutor extends TaskExecutor {

	/**
     * 表示立即执行的常量
     * 超时时间
     */
	long TIMEOUT_IMMEDIATE = 0;

	/**
     * 表示没有时间限制的常数
     */
	long TIMEOUT_INDEFINITE = Long.MAX_VALUE;


	/**
	 * 获取执行任务.
	 * @param task 要执行的Runnable（不可为null）
	 * @param startTimeout 任务开始的持续时间(毫秒)。这是作为执行程序的提示，允许优先处理当前任务。
     *                     典型的值是TIMEOUT_IMMEDIATE或TIMEOUT_INDEFINITE(execute(Runnable)使用的缺省值)。
	 */
	void execute(Runnable task, long startTimeout);

	/**
	 * 提交要执行的Runnable任务，并接收代表该任务的Future
     * Future将在完成后返回空结果
	 * @param task 要执行的Runnable（不可为null）
	 */
	Future<?> submit(Runnable task);

	/**
	 * 提交要执行的Callable任务，接收代表该任务的Future
	 * 在以后完成时返回可调用的结果
	 * @param task 要执行的Callable（不可为null）
	 */
	<T> Future<T> submit(Callable<T> task);

}