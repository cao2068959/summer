package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.config.BeanPostProcessor;

public interface SmartInstantiationAwareBeanPostProcessor extends BeanPostProcessor {

    Object getEarlyBeanReference(Object exposedObject, String beanName);
}
