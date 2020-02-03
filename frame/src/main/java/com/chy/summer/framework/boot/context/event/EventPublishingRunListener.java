package com.chy.summer.framework.boot.context.event;

import com.chy.summer.framework.boot.SummerApplication;
import com.chy.summer.framework.boot.listeners.SummerApplicationRunListener;
import com.chy.summer.framework.context.ConfigurableApplicationContext;
import com.chy.summer.framework.context.event.ApplicationListener;
import com.chy.summer.framework.context.event.SimpleApplicationEventMulticaster;
import com.chy.summer.framework.core.evn.ConfigurableEnvironment;

public class EventPublishingRunListener implements SummerApplicationRunListener {


    private final SummerApplication application;
    private final String[] args;
    private final SimpleApplicationEventMulticaster initialMulticaster;

    public EventPublishingRunListener(SummerApplication application, String[] args) {
        this.application = application;
        this.args = args;
        this.initialMulticaster = new SimpleApplicationEventMulticaster();
        for (ApplicationListener<?> listener : application.getListeners()) {
            this.initialMulticaster.addApplicationListener(listener);
        }
    }

    @Override
    public void starting() {

    }


    /**
     * 触发了 ApplicationEnvironmentPreparedEvent 事件, 在这个事件里会去加载配置文件
     * @param environment
     */
    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        initialMulticaster.multicastEvent(new ApplicationEnvironmentPreparedEvent(application, args, environment));
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {

    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {

    }

    @Override
    public void started(ConfigurableApplicationContext context) {

    }

    @Override
    public void running(ConfigurableApplicationContext context) {

    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {

    }
}
