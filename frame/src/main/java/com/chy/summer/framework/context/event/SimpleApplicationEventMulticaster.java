package com.chy.summer.framework.context.event;

import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.core.ResolvableType;

import java.util.concurrent.Executor;

public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {


    private final ConfigurableListableBeanFactory beanFactory;

    public SimpleApplicationEventMulticaster(ConfigurableListableBeanFactory beanFactory) {
        super(beanFactory);
        this.beanFactory = beanFactory;
    }


    /**
     * 执行广播的事件了
     * @param event
     */
    @Override
    public void multicastEvent(ApplicationEvent event) {
        multicastEvent(event, ResolvableType.forClass(event.getClass()));
    }

    private void multicastEvent(ApplicationEvent event, ResolvableType eventType) {
        ResolvableType type = (eventType != null ? eventType : ResolvableType.forClass(event.getClass()));
        for (final ApplicationListener<?> listener : getApplicationListeners(event, type)) {
            Executor executor = getTaskExecutor();
            if (executor != null) {
                executor.execute(() -> invokeListener(listener, event));
            }
            else {
                invokeListener(listener, event);
            }
        }
    }
}
