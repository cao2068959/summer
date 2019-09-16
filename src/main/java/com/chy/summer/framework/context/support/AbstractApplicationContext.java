package com.chy.summer.framework.context.support;

import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.support.DefaultListableBeanFactory;
import com.chy.summer.framework.context.ApplicationContext;

public abstract class AbstractApplicationContext   implements ApplicationContext {

    public void refresh(){

    };

    @Override
    public Object getBean(String name) {
        return null;
    }
}
