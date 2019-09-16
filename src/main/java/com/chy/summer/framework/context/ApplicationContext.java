package com.chy.summer.framework.context;

import com.chy.summer.framework.beans.BeanFactory;

public interface ApplicationContext extends BeanFactory {

    ApplicationContext getParent();

}
