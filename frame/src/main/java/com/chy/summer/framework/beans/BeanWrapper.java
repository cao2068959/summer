package com.chy.summer.framework.beans;

public interface BeanWrapper {
    /**
     * 返回正真的未包装过的实例对象
     * @return
     */
    Object getWrappedInstance();

    /**
     * 返回 实例的类型
     * @return
     */
    Class<?> getWrappedClass();
}
