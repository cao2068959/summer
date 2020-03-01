package com.chy.summer.framework.core.evn;

import com.chy.summer.framework.boot.SummerApplication;

public interface EnvironmentPostProcessor {

    void postProcessEnvironment(ConfigurableEnvironment environment, SummerApplication application);
}
