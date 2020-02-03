package com.chy.summer.framework.boot.context.event;

import com.chy.summer.framework.context.event.ApplicationEvent;
import com.chy.summer.framework.context.event.ApplicationListener;
import com.chy.summer.framework.core.ordered.Ordered;

public interface SmartApplicationListener extends ApplicationListener<ApplicationEvent>, Ordered {


    /**
     * 判断这个事件的类型是否相同
     * @param eventType
     * @return
     */
    boolean supportsEventType(Class<? extends ApplicationEvent> eventType);


    /**
     * 判断事件的来源是否相同
     * @param sourceType
     * @return
     */
    default boolean supportsSourceType(Class<?> sourceType) {
        return true;
    }


    @Override
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
