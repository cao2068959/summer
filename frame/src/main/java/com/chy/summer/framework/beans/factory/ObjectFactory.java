package com.chy.summer.framework.beans.factory;

import com.chy.summer.framework.exception.BeansException;

public interface ObjectFactory<T> {

    T getObject() throws BeansException;
}
