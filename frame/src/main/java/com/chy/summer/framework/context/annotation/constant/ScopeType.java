package com.chy.summer.framework.context.annotation.constant;

public enum ScopeType {

    /**
     * 单例
     */
    SINGLETON,
    /**
     * 原型
     */
    PROTOTYPE,
    /**
     *  不同的session下有不同的 对象
     */
    SESSION,

    /**
     * 对象的生存周期就是一次请求的周期
     */
    REQUEST;



}
