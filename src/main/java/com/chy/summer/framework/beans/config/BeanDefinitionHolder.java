package com.chy.summer.framework.beans.config;

/**
 * beanDefinition 的持有者类
 */
public class BeanDefinitionHolder {

    private  BeanDefinition beanDefinition;

    private  String beanName;

    private  String[] aliases;



    public BeanDefinition getBeanDefinition() {
        return beanDefinition;
    }

    public void setBeanDefinition(BeanDefinition beanDefinition) {
        this.beanDefinition = beanDefinition;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String[] getAliases() {
        return aliases;
    }

    public void setAliases(String[] aliases) {
        this.aliases = aliases;
    }
}
