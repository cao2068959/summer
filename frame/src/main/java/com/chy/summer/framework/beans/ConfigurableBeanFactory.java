package com.chy.summer.framework.beans;

import com.chy.summer.framework.beans.support.RootBeanDefinition;
import com.sun.istack.internal.Nullable;

public interface ConfigurableBeanFactory {
    /**
     * 获取这个工厂的类装入器以加载bean类
     */
    @Nullable
    ClassLoader getBeanClassLoader();

    boolean isCurrentlyInCreation(String beanName);

    Object getSingletonMutex();
    //TODO 尚未完成
}