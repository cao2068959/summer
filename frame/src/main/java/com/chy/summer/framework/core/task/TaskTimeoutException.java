package com.chy.summer.framework.core.task;

/**
 * 当AsyncTaskExecutor由于指定的超时而拒绝接受给定任务以执行时引发的异常
 */
public class TaskTimeoutException extends TaskRejectedException {

	/**
	 * 用指定的详细信息创建一个新的TaskTimeoutException异常，没有保留根本原因。
	 * 	 * @param msg 错误的详细信息
	 */
	public TaskTimeoutException(String msg) {
		super(msg);
	}

	/**
	 * 使用指定的详细消息和给定的根本原因创建一个新的TaskTimeoutException
	 * @param msg 错误的详细信息
	 * @param cause 根本原因
	 */
	public TaskTimeoutException(String msg, Throwable cause) {
		super(msg, cause);
	}

}