package com.chy.summer.framework.beans;

public interface BeanFactory {

    /**
     * 工厂bean的前缀，用于将工厂bean与由其创建的bean区别开。
     * 例如，如果名为myJndiObject的bean是FactoryBean，
     * 使其变成＆myJndiObject的方式获取，将返回FactoryBean，而不是工厂返回的实例。
     */
    String FACTORY_BEAN_PREFIX = "&";

    Object getBean(String name);
}
