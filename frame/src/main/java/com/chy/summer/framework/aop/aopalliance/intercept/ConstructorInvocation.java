package com.chy.summer.framework.aop.aopalliance.intercept;

import java.lang.reflect.Constructor;

/**
 * 构造器调用的接口，获取aop连接点的相关对象
 */
public interface ConstructorInvocation extends Invocation {

    /**
     * 获取调用的构造器
     * {@link Joinpoint#getStaticPart()} 与这个方法类似，结果相同
     */
    Constructor<?> getConstructor();
}