package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.BeanFactory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractBeanFactory implements BeanFactory {

    private final Set<String> alreadyCreated = Collections.newSetFromMap(new ConcurrentHashMap<>(256));

    /**
     * 判断是否已经开始创建 bean 对象了
     * @return
     */
    protected boolean hasBeanCreationStarted() {
        return !this.alreadyCreated.isEmpty();
    }

}
