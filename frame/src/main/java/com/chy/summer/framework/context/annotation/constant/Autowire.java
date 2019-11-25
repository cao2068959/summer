package com.chy.summer.framework.context.annotation.constant;

public enum Autowire {

    NO,

    BY_NAME,

    BY_TYPE;

    public boolean isAutowire() {
        return (this == BY_NAME || this == BY_TYPE);
    }
}
