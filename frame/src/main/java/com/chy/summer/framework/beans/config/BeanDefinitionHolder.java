package com.chy.summer.framework.beans.config;

import com.chy.summer.framework.beans.BeanNameAware;
import com.chy.summer.framework.beans.support.RootBeanDefinition;
import com.chy.summer.framework.util.Assert;

/**
 * beanDefinition 的持有者类
 */
public class BeanDefinitionHolder  implements BeanNameAware {

    private  BeanDefinition beanDefinition;

    private  String beanName;

    private  String[] aliases;

    public BeanDefinitionHolder(BeanDefinition definition, String beanName) {
        this.beanDefinition = definition;
        this.beanName = beanName;
    }

    public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName, String[] aliases) {
        this.beanDefinition = beanDefinition;
        this.beanName = beanName;
        this.aliases = aliases;
    }


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
