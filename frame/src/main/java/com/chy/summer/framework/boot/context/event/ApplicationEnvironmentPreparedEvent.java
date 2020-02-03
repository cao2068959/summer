package com.chy.summer.framework.boot.context.event;


import com.chy.summer.framework.boot.SummerApplication;
import com.chy.summer.framework.core.evn.ConfigurableEnvironment;
import lombok.Getter;

public class ApplicationEnvironmentPreparedEvent extends SummerApplicationEvent {


    @Getter
    private final ConfigurableEnvironment configurableEnvironment;

    public ApplicationEnvironmentPreparedEvent(SummerApplication application, String[] args,
                                               ConfigurableEnvironment configurableEnvironment) {
        super(application, args);
        this.configurableEnvironment = configurableEnvironment;

    }




}
