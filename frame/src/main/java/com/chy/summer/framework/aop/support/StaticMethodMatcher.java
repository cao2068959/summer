package com.chy.summer.framework.aop.support;

import com.chy.summer.framework.aop.MethodMatcher;
import javax.annotation.Nullable;

import java.lang.reflect.Method;

public abstract class StaticMethodMatcher implements MethodMatcher {

	@Override
	public final boolean isRuntime() {
		return false;
	}

	@Override
	public final boolean matches(Method method, @Nullable Class<?> targetClass, Object... args) {
		//永远不要调用，因为isRuntime（）返回false,请在子类中调用
		throw new UnsupportedOperationException("MethodMatcher非法使用");
	}

}