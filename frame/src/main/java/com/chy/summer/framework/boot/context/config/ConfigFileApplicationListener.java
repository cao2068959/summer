package com.chy.summer.framework.boot.context.config;


import com.chy.summer.framework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import com.chy.summer.framework.boot.context.event.SmartApplicationListener;
import com.chy.summer.framework.context.event.ApplicationEvent;

/**
 * 配置文件的 事件监听处理器, 当响应ApplicationEnvironmentPreparedEvent 事件的时候会去 加载配置文件
 */
public class ConfigFileApplicationListener implements SmartApplicationListener {


    /**
     * 通过事件类型去匹配是否是同一个事件类型
     * @param eventType
     * @return
     */
    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return ApplicationEnvironmentPreparedEvent.class.isAssignableFrom(eventType);
    }


    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        System.out.println("------------> 执行了事件 ConfigFileApplicationListener");
    }
}
