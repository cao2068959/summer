package com.chy.summer.framework.beans.config;


import com.chy.summer.framework.beans.BeanFactory;

public interface ConfigurableListableBeanFactory extends BeanFactory {

    /**
     * 用了设置忽略类型
     */
    void ignoreDependencyType(Class<?> type);

    /**
     * 用了设置忽略接口
     */
    void ignoreDependencyInterface(Class<?> ifc);

}
