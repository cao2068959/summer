package com.chy.summer.framework.beans;

import com.chy.summer.framework.beans.support.RootBeanDefinition;
import com.chy.summer.framework.util.StringValueResolver;
import com.sun.istack.internal.Nullable;

public interface ConfigurableBeanFactory {
    /**
     * 获取这个工厂的类装入器以加载bean类
     */
    @Nullable
    ClassLoader getBeanClassLoader();

    boolean isCurrentlyInCreation(String beanName);

    Object getSingletonMutex();

    Object getBean(String beanName);

//    void destroyScopedBean(String beanName);
    //TODO 尚未完成


    void addEmbeddedValueResolver(StringValueResolver valueResolver);

    boolean hasEmbeddedValueResolver();

    /**
     * 解析嵌入的表达式的值 比如 ${com.chy} 这里解析的就是 com.chy 对应的值
     *
     * @param value
     * @return
     */
    String resolveEmbeddedValue(@Nullable String value);

}