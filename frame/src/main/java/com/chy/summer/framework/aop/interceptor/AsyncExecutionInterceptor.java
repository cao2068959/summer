package com.chy.summer.framework.aop.interceptor;

import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;
import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.chy.summer.framework.aop.support.AopUtils;
import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.core.BridgeMethodResolver;
import com.chy.summer.framework.core.ordered.Ordered;
import com.chy.summer.framework.core.task.AsyncTaskExecutor;
import com.chy.summer.framework.core.task.SimpleAsyncTaskExecutor;
import com.chy.summer.framework.util.ClassUtils;
import javax.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * 使用给定的AsyncTaskExecutor异步处理方法调用的AOP Alliance MethodInterceptor。通常与异步注释一起使用
 *
 * 在目标方法签名方面，支持任何参数类型，但是，返回类型被限制为void或Future。
 * 在后一种情况下，从代理返回的Future句柄将是一个实际的异步Future，可用于跟踪异步方法执行的结果。
 * 但是，由于目标方法需要实现相同的签名，因此它必须返回一个临时的Future句柄，该句柄只通过它传递返回值(如AsyncResult或EJB 3.1的AsyncResult)
 *
 * 当返回类型为Future时，调用者可以访问和管理执行期间抛出的任何异常。
 * 但是，对于Void返回类型，无法将此类异常传回。 在这种情况下，可以注册AsyncUncaughtExceptionHandler来处理此类异常。
 */
public class AsyncExecutionInterceptor extends AsyncExecutionAspectSupport implements MethodInterceptor, Ordered {

	/**
	 * 使用默认的AsyncUncaughtExceptionHandler创建一个新实例
	 * @param defaultExecutor 委托给的执行者（通常是AsyncTaskExecutor或ExecutorService）
	 */
	public AsyncExecutionInterceptor(@Nullable Executor defaultExecutor) {
		super(defaultExecutor);
	}

	/**
	 * 创建一个新的AsyncExecutionInterceptor实例
	 * @param defaultExecutor 委托给的执行者（通常是AsyncTaskExecutor或ExecutorService）
	 * @param exceptionHandler 使用的AsyncUncaughtExceptionHandler
	 */
	public AsyncExecutionInterceptor(@Nullable Executor defaultExecutor, AsyncUncaughtExceptionHandler exceptionHandler) {
		super(defaultExecutor, exceptionHandler);
	}


	/**
	 * 拦截给定的方法调用，将方法的实际调用提交给正确的任务执行器，并立即返回给调用者
	 * @param invocation 拦截并调用异步的方法
	 */
	@Override
	@Nullable
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		//获取真正的目标对象
		Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);
		//获取具体的方法
		Method specificMethod = ClassUtils.getMostSpecificMethod(invocation.getMethod(), targetClass);
		final Method userDeclaredMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
		//查找执行器
		AsyncTaskExecutor executor = determineAsyncExecutor(userDeclaredMethod);
		if (executor == null) {
			throw new IllegalStateException("在AsyncExecutionInterceptor上没有指定执行器，也没有设置默认的执行器");
		}
		//异步调用方法
		Callable<Object> task = () -> {
			try {
				Object result = invocation.proceed();
				if (result instanceof Future) {
					return ((Future<?>) result).get();
				}
			}
			catch (ExecutionException ex) {
				handleError(ex.getCause(), userDeclaredMethod, invocation.getArguments());
			}
			catch (Throwable ex) {
				handleError(ex, userDeclaredMethod, invocation.getArguments());
			}
			return null;
		};
		//使用选定的执行器实际执行给定任务的委托
		return doSubmit(task, executor, invocation.getMethod().getReturnType());
	}

	/**
	 * 子类重写以提供对提取限定符信息的支持，例如 通过给定方法上的注释
	 */
	@Override
	@Nullable
	protected String getExecutorQualifier(Method method) {
		return null;
	}

	/**
	 * 此实现在上下文中搜索唯一的TaskExecutor bean，否则搜索名为“TaskExecutor”的Executor bean。
	 * 如果两者都不可解析(例如，如果根本没有配置BeanFactory)，则此实现将返回到新创建的SimpleAsyncTaskExecutor实例以供本地使用(如果没有找到默认值)
	 */
	@Override
	@Nullable
	protected Executor getDefaultExecutor(@Nullable BeanFactory beanFactory) {
		Executor defaultExecutor = super.getDefaultExecutor(beanFactory);
		return (defaultExecutor != null ? defaultExecutor : new SimpleAsyncTaskExecutor());
	}

	/**
	 * 获取此对象的顺序值
	 */
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}