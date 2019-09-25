package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.context.annotation.constant.ScopeType;
import com.chy.summer.framework.context.annotation.constant.ScopedProxyMode;

public class ScopeMetadata {

    private ScopeType scopeName = ScopeType.SINGLETON;

    private ScopedProxyMode scopedProxyMode = ScopedProxyMode.NO;

    public ScopeType getScopeName() {
        return scopeName;
    }

    public void setScopeName(ScopeType scopeName) {
        this.scopeName = scopeName;
    }

    public ScopedProxyMode getScopedProxyMode() {
        return scopedProxyMode;
    }

    public void setScopedProxyMode(ScopedProxyMode scopedProxyMode) {
        this.scopedProxyMode = scopedProxyMode;
    }
}
