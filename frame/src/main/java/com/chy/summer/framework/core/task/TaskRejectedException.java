package com.chy.summer.framework.core.task;

import java.util.concurrent.RejectedExecutionException;

/**
 * TaskExecutor拒绝接受给定任务以执行时引发的异常
 */
public class TaskRejectedException extends RejectedExecutionException {

	/**
	 * 用指定的详细信息创建一个新的TaskRejectedException异常，没有保留根本原因。
	 * @param msg 错误的详细信息
	 */
	public TaskRejectedException(String msg) {
		super(msg);
	}

	/**
	 * 使用指定的详细消息和给定的根本原因创建一个新的TaskRejectedException。
	 * @param msg 错误的详细信息
	 * @param cause 根本原因
	 */
	public TaskRejectedException(String msg, Throwable cause) {
		super(msg, cause);
	}

}