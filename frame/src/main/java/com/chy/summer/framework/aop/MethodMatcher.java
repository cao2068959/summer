package com.chy.summer.framework.aop;

import com.sun.istack.internal.Nullable;

import java.lang.reflect.Method;

/**
 * 方法匹配器
 * MethodMatcher接口通过重载定义了两个matches()方法，两个参数的matches()被称为静态匹配（非严格匹配）
 * 三个参数的matches()方法需要在运行时动态的对参数的类型进行匹配
 * 两个方法的分界线就是boolean isRuntime()方法，
 * 进行匹配时先用两个参数的matches()方法进行匹配，若匹配成功，
 * 则检查boolean isRuntime()的返回值，若为true，则调用三个参数的matches()方法进行匹配
 */
public interface MethodMatcher {

	/**
	 * 执行静态匹配，看给定的方法是否匹配
     * 返回false时，调用isRuntime()，如果isRuntime()方法也返回false，则表示没有匹配到对象
	 */
	boolean matches(Method method, @Nullable Class<?> targetClass);

	/**
	 * 这个方法匹配器是否动态的，等于true的时候调用2个参数的matches()
	 */
	boolean isRuntime();

	/**
	 * 执行动态匹配，该匹配必须静态匹配之后才会执行。
	 */
	boolean matches(Method method, @Nullable Class<?> targetClass, Object... args);


	/**
	 * 匹配所有方法的规范
	 */
	MethodMatcher TRUE = TrueMethodMatcher.INSTANCE;

}