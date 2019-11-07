package com.chy.summer.framework.aop.interceptor;

import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.BeanFactoryAware;
import com.chy.summer.framework.core.task.AsyncListenableTaskExecutor;
import com.chy.summer.framework.core.task.AsyncTaskExecutor;
import com.chy.summer.framework.core.task.TaskExecutor;
import com.chy.summer.framework.core.task.support.TaskExecutorAdapter;
import com.chy.summer.framework.exception.NoSuchBeanDefinitionException;
import com.chy.summer.framework.exception.NoUniqueBeanDefinitionException;
import com.chy.summer.framework.util.BeanFactoryAnnotationUtils;
import com.chy.summer.framework.util.ReflectionUtils;
import com.chy.summer.framework.util.StringUtils;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 异步方法执行切面的基类，如AnnotationAsyncExecutionInterceptor或AnnotationAsyncExecutionAspect。
 */
@Slf4j
public abstract class AsyncExecutionAspectSupport implements BeanFactoryAware {

	/**
	 * 获取TaskExecutor bean的默认名称:“TaskExecutor”
	 */
	public static final String DEFAULT_TASK_EXECUTOR_BEAN_NAME = "taskExecutor";

	private final Map<Method, AsyncTaskExecutor> executors = new ConcurrentHashMap<>(16);

	@Nullable
	private volatile Executor defaultExecutor;

	private AsyncUncaughtExceptionHandler exceptionHandler;

	@Nullable
	private BeanFactory beanFactory;


	/**
	 * 使用默认的AsyncUncaughtExceptionHandler创建一个新实例
	 * @param defaultExecutor 委托的执行器(通常是AsyncTaskExecutor或ExecutorService)，
     *                        除非通过异步方法上的限定符请求了更特定的执行器，在这种情况下，执行器将在调用时针对封闭的bean工厂进行查找
	 */
	public AsyncExecutionAspectSupport(@Nullable Executor defaultExecutor) {
		this(defaultExecutor, new SimpleAsyncUncaughtExceptionHandler());
	}

	/**
	 * 使用给定的异常处理程序创建一个新的AsyncExecutionAspectSupport.
	 * @param defaultExecutor 委托的执行器(通常是AsyncTaskExecutor或ExecutorService)，
     *                         除非通过异步方法上的限定符请求了更特定的执行器，在这种情况下，执行器将在调用时针对封闭的bean工厂进行查找
	 * @param exceptionHandler 使用的AsyncUncaughtExceptionHandler
	 */
	public AsyncExecutionAspectSupport(@Nullable Executor defaultExecutor, AsyncUncaughtExceptionHandler exceptionHandler) {
		this.defaultExecutor = defaultExecutor;
		this.exceptionHandler = exceptionHandler;
	}


	/**
	 * 提供执行异步方法时使用的执行器
	 * @param defaultExecutor 委托的执行器(通常是AsyncTaskExecutor或ExecutorService)，
	 *                        除非通过异步方法上的限定符请求了更特定的执行器，在这种情况下，执行器将在调用时针对封闭的bean工厂进行查找
	 */
	public void setExecutor(Executor defaultExecutor) {
		this.defaultExecutor = defaultExecutor;
	}

	/**
	 * 提供AsyncUncaughtExceptionHandler来处理通过调用具有空返回类型的异步方法引发的异常
	 */
	public void setExceptionHandler(AsyncUncaughtExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	/**
	 * 设置在通过限定符查找执行器或依赖默认执行器查找算法时使用的BeanFactory
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}


	/**
	 * 确定执行给定方法时要使用的特定执行器
	 * 最好应返回一个AsyncListenableTaskExecutor实现
	 */
	@Nullable
	protected AsyncTaskExecutor determineAsyncExecutor(Method method) {
		AsyncTaskExecutor executor = this.executors.get(method);
		if (executor == null) {
			Executor targetExecutor;
			String qualifier = getExecutorQualifier(method);
			if (StringUtils.hasLength(qualifier)) {
				targetExecutor = findQualifiedExecutor(this.beanFactory, qualifier);
			}
			else {
				targetExecutor = this.defaultExecutor;
				if (targetExecutor == null) {
					synchronized (this.executors) {
						if (this.defaultExecutor == null) {
							this.defaultExecutor = getDefaultExecutor(this.beanFactory);
						}
						targetExecutor = this.defaultExecutor;
					}
				}
			}
			if (targetExecutor == null) {
				return null;
			}
			executor = (targetExecutor instanceof AsyncListenableTaskExecutor ?
					(AsyncListenableTaskExecutor) targetExecutor : new TaskExecutorAdapter(targetExecutor));
			this.executors.put(method, executor);
		}
		return executor;
	}

	/**
	 * 返回要在执行给定异步方法时使用的执行器的限定符或bean名，通常以注释属性的形式指定。
	 * 返回空字符串或null表示没有指定特定的执行器，应该使用setExecutor(executor)的默认执行器
	 * @param method 检查执行器限定符元数据的方法
	 */
	@Nullable
	protected abstract String getExecutorQualifier(Method method);

	/**
	 * 检索给定限定符的目标执行器
	 * @param qualifier 要解析的限定符
	 */
	@Nullable
	protected Executor findQualifiedExecutor(@Nullable BeanFactory beanFactory, String qualifier) {
		if (beanFactory == null) {
			throw new IllegalStateException("必须在" + getClass().getSimpleName() +
					"上设置BeanFactory才能访问合格的执行器 '" + qualifier + "'");
		}
		return BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, Executor.class, qualifier);
	}

	/**
	 * 检索或构建此建议实例的默认执行程序。从这里返回的执行程序将被缓存进一步使用。
	 * 默认实现在上下文中搜索唯一的TaskExecutor bean，否则搜索名为“TaskExecutor”的Executor bean。如果两者都不可解，则此实现将返回null
	 * @param beanFactory 用于默认执行器查找的BeanFactory
	 */
	@Nullable
	protected Executor getDefaultExecutor(@Nullable BeanFactory beanFactory) {
		if (beanFactory != null) {
			try {
				return beanFactory.getBean("taskExecutor",TaskExecutor.class);
			}
			catch (NoUniqueBeanDefinitionException ex) {
				log.debug("找不到唯一的TaskExecutor bean", ex);
				try {
					return beanFactory.getBean(DEFAULT_TASK_EXECUTOR_BEAN_NAME, Executor.class);
				}
				catch (NoSuchBeanDefinitionException ex2) {
					if (log.isInfoEnabled()) {
						log.info("在上下文中发现多个TaskExecutor bean，且没有一个名为“TaskExecutor”。" +
								"标记其中之一为主要或命名为'taskExecutor'(可能作为别名)，以便使用它进行异步处理:" + ex.getBeanNamesFound());
					}
				}
			}
			catch (NoSuchBeanDefinitionException ex) {
				log.debug("无法找到默认的TaskExecutor bean", ex);
				try {
					return beanFactory.getBean(DEFAULT_TASK_EXECUTOR_BEAN_NAME, Executor.class);
				}
				catch (NoSuchBeanDefinitionException ex2) {
					log.info("找不到用于异步处理的任务执行器bean：既没有TaskExecutor类型的bean，也没有名为'taskExecutor'的bean");
				}
			}
		}
		return null;
	}


	/**
	 * 使用选定的执行器实际执行给定任务的委托
	 * @param task 要执行的任务
	 * @param executor 选定的执行器
	 * @param returnType 声明的返回类型（可能是Future变量）
	 */
	@Nullable
	protected Object doSubmit(Callable<Object> task, AsyncTaskExecutor executor, Class<?> returnType) {
		if (CompletableFuture.class.isAssignableFrom(returnType)) {
			return CompletableFuture.supplyAsync(() -> {
				try {
					return task.call();
				}
				catch (Throwable ex) {
					throw new CompletionException(ex);
				}
			}, executor);
		}
		else if (ListenableFuture.class.isAssignableFrom(returnType)) {
			return ((AsyncListenableTaskExecutor) executor).submitListenable(task);
		}
		else if (Future.class.isAssignableFrom(returnType)) {
			return executor.submit(task);
		}
		else {
			executor.submit(task);
			return null;
		}
	}

	/**
	 * 处理异步调用指定方法时引发的致命错误
	 * 如果方法的返回类型是Future对象，则可以通过将其抛出更高级别来传播原始异常
	 * 但是，对于所有其他情况，该异常将不会发送回客户端
	 * 在这种情况下，将使用当前的AsyncUncaughtExceptionHandler来管理此类异常
	 * @param ex 要处理的异常
	 * @param method 调用的方法
	 * @param params 用于调用方法的参数
	 */
	protected void handleError(Throwable ex, Method method, Object... params) throws Exception {
		if (Future.class.isAssignableFrom(method.getReturnType())) {
			ReflectionUtils.rethrowException(ex);
		}
		else {
			//无法使用默认执行器将异常传输给调用方
			try {
				this.exceptionHandler.handleUncaughtException(ex, method, params);
			}
			catch (Throwable ex2) {
				log.error("异步方法'" + method.toGenericString() + "'的异常处理程序本身抛出意外异常", ex2);
			}
		}
	}

}