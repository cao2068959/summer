package com.chy.summer.framework.context.event;

import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.core.ResolvableType;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

@Slf4j
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {


    private final ConfigurableListableBeanFactory beanFactory;

    private Executor taskExecutor;

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

        //根据事件类型 来拿到对应事件下面的所有 监听器
        for (final ApplicationListener<?> listener : getApplicationListeners(event, type)) {
            Executor executor = getTaskExecutor();
            //这里如果有 executor 那么就讲会在线程中执行 监听回调
            if (executor != null) {
                executor.execute(() -> invokeListener(listener, event));
            }
            else {
                //这里是同步的方式执行
                invokeListener(listener, event);
            }
        }
    }

    protected Executor getTaskExecutor() {
        return this.taskExecutor;
    }

    protected void invokeListener(ApplicationListener listener, ApplicationEvent event) {
        log.debug("事件 [%s] 触发了监听器 [%s] 执行",event,listener);
        listener.onApplicationEvent(event);
    }

}
