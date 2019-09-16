package com.chy.summer.framework.web.servlet.context.support;

import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.context.ApplicationContext;
import com.chy.summer.framework.context.support.AbstractApplicationContext;

public class GenericWebApplicationContext extends AbstractApplicationContext implements BeanDefinitionRegistry {


    @Override
    public ApplicationContext getParent() {
        return null;
    }



}
