package com.chy.summer.framework.aop;

import com.chy.summer.framework.aop.aopalliance.intercept.MethodInterceptor;

/**
 * 这是一种特别的advice，在不改变原有方法的基础上却可以增加新的方法
 */
public interface IntroductionInterceptor extends MethodInterceptor, DynamicIntroductionAdvice {

}