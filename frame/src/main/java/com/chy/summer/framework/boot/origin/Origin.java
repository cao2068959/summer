package com.chy.summer.framework.boot.origin;

public interface Origin {

    static Origin from(Object source) {
        if (source instanceof Origin) {
            return (Origin) source;
        }
        Origin origin = null;
        if (source instanceof OriginProvider) {
            origin = ((OriginProvider) source).getOrigin();
        }
        if (origin == null && source instanceof Throwable) {
            return from(((Throwable) source).getCause());
        }
        return origin;
    }


}
