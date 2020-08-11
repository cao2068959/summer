package com.chy.summer.framework.beans.config;


/**
 * 属性访问器
 * beanDefinition是描述一个对象的所有行为的话,访问一个对象里所有属性也属于 beanDefinition 该做的事情
 * 这个接口定义的就是如何去 获取/设置/删除 属性
 */
public interface AttributeAccessor {

    /**
     * 设置一个属性
     * @param name
     * @param value
     */
    void setAttribute(String name, Object value);

    /**
     * 获取某个属性
     * @param name
     * @return
     */

    Object getAttribute(String name);


    /**
     * 删除某个属性
     * @param name
     * @return
     */
    Object removeAttribute(String name);


    /**
     * 判断有没某个属性
     *
     * @param name
     * @return
     */
    boolean hasAttribute(String name);

    /**
     * 获取所有属性的名称
     * @return
     */
    String[] attributeNames();
}
