package com.chy.summer.framework.core.evn;

import com.chy.summer.framework.boot.origin.Origin;
import com.chy.summer.framework.boot.origin.OriginTrackedValue;
import com.chy.summer.framework.core.evn.propertysource.MapPropertySource;
import lombok.Getter;

import java.util.Map;

public class OriginTrackedMapPropertySource extends MapPropertySource {

    @Getter
    private final boolean immutable;

    public OriginTrackedMapPropertySource(String name, Map<String, Object> source,Boolean immutable) {
        super(name, source);
        this.immutable = immutable;
    }

    @Override
    public Object getProperty(String name) {
        Object value = super.getProperty(name);
        if (value instanceof OriginTrackedValue) {
            return ((OriginTrackedValue) value).getValue();
        }
        return value;
    }

    public Origin getOrigin(String name) {
        Object value = super.getProperty(name);
        if (value instanceof OriginTrackedValue) {
            return ((OriginTrackedValue) value).getOrigin();
        }
        return null;
    }
}
