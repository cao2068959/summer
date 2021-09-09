package com.chy.summer.framework.context;


import com.chy.summer.framework.beans.Aware;
import com.chy.summer.framework.core.evn.Environment;

public interface EnvironmentAware  extends Aware {

    void setEnvironment(Environment environment);

}
