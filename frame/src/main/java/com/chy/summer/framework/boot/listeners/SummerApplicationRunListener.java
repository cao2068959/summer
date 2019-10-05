package com.chy.summer.framework.boot.listeners;

import com.chy.summer.framework.context.ConfigurableApplicationContext;
import com.chy.summer.framework.core.evn.ConfigurableEnvironment;

public interface SummerApplicationRunListener {

    void starting();

    void environmentPrepared(ConfigurableEnvironment environment);

    void contextPrepared(ConfigurableApplicationContext context);


    void contextLoaded(ConfigurableApplicationContext context);


    void started(ConfigurableApplicationContext context);


    void running(ConfigurableApplicationContext context);


    void failed(ConfigurableApplicationContext context, Throwable exception);

}
