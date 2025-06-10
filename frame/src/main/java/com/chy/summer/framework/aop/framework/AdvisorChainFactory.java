package com.chy.summer.framework.aop.framework;


import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;

/**
 * advisor链的工厂接口
 */
public interface AdvisorChainFactory {

	/**
	 * 获取给定advisor链配置的MethodInterceptor对象列表
	 * @param config advised对象形式的AOP配置
	 * @param method 代理方法
	 * @param targetClass 目标类（可以为null，标识没有目标对象的代理，在这种情况下，方法的声明类是次佳选择）
	 */
	List<Object> getInterceptorsAndDynamicInterceptionAdvice(Advised config, Method method, @Nullable Class<?> targetClass);

}