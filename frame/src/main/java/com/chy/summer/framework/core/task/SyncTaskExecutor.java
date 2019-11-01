package com.chy.summer.framework.core.task;

import com.chy.summer.framework.util.Assert;

import java.io.Serializable;

/**
 * TaskExecutord的实现，可在调用线程中同步执行每个任务
 */
public class SyncTaskExecutor implements TaskExecutor, Serializable {

	/**
	 * 通过直接调用它的Runnable的run()方法来同步执行给定任务
	 */
	@Override
	public void execute(Runnable task) {
		Assert.notNull(task, "Runnable不可为空");
		task.run();
	}

}