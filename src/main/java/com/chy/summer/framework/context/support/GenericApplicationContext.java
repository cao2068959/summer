package com.chy.summer.framework.context.support;

import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.context.ApplicationContext;

public class GenericApplicationContext extends AbstractApplicationContext implements BeanDefinitionRegistry {

    @Override
    public ApplicationContext getParent() {
        return null;
    }
}
