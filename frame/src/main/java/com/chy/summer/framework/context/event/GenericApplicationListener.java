package com.chy.summer.framework.context.event;

import com.chy.summer.framework.core.ResolvableType;
import com.chy.summer.framework.core.ordered.Ordered;
import javax.annotation.Nullable;

public interface GenericApplicationListener extends ApplicationListener<ApplicationEvent>, Ordered {

    /**
     * 这个监听器 支持什么事件类型 的事件
     * @param eventType
     * @return
     */
    boolean supportsEventType(ResolvableType eventType);


    /**
     * 从事件的来源 上判断是否符合这个监听器的规则
     * @param sourceType
     * @return
     */
    boolean supportsSourceType(@Nullable Class<?> sourceType);
}
