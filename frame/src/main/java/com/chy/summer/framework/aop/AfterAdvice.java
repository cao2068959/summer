package com.chy.summer.framework.aop;

import com.chy.summer.framework.aop.aopalliance.Advice;

/**
 * 后置通知，与前置通知用法相似，但是功能是相反的，后置通知是指在目标方法被调用后执行
 */
public interface AfterAdvice extends Advice {

}