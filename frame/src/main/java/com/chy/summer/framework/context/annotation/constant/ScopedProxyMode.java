package com.chy.summer.framework.context.annotation.constant;

public enum  ScopedProxyMode {


    /**
     * 默认
     */
    DEFAULT,

    /**
     * 不使用作用域代理
     */
    NO,

    /**
     *  JDK默认代理
     */
    INTERFACES,

    /**
     *  CGLIB
     */
    TARGET_CLASS;

}
