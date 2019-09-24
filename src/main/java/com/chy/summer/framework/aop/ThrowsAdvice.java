package com.chy.summer.framework.aop;

/**
 * 用于抛出异常通知的标记接口
 *
 * 该接口上没有任何方法，但是实现了这个接口的类必须至少实现以下4个方法中的一个，否则程序将会异常
 * public void afterThrowing(Exception ex)
 * public void afterThrowing(RemoteException)
 * public void afterThrowing(Method method, Object[] args, Object target, Exception ex)
 * public void afterThrowing(Method method, Object[] args, Object target, ServletException ex)
 */
public interface ThrowsAdvice extends AfterAdvice {

}
