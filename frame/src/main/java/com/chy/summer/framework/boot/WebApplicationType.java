package com.chy.summer.framework.boot;

public enum  WebApplicationType {
    /**
     * 没有任何的web容器
     */
    NONE,

    /**
     * servlet 容器 tomcat 等
     */
    SERVLET,

    /**
     * 用原生的 web容器
     */
    REACTIVE

}
