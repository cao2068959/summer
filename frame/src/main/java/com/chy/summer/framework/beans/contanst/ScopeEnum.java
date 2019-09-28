package com.chy.summer.framework.beans.contanst;

public enum ScopeEnum {
    /**
     * 单例
     */
    SINGLETON(1,"singleton"),
    /**
     * 原型模式
     */
    PROTOTYPE(2,"prototype");

    private Integer code;
    private String name;

    ScopeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
