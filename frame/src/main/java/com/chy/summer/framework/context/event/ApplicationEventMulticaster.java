package com.chy.summer.framework.context.event;

public interface ApplicationEventMulticaster {


    void addApplicationListener(ApplicationListener<?> listener);


    void addApplicationListenerBean(String listenerBeanName);


    void removeApplicationListener(ApplicationListener<?> listener);


    void removeApplicationListenerBean(String listenerBeanName);


    void removeAllListeners();


    void multicastEvent(ApplicationEvent event);


}
