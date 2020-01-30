package com.chy.summer.framework.core.evn.propertysource;

import java.util.Map;

public class MapPropertySource extends PropertySource<Map<String, Object>> {


    public MapPropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }


    @Override
    public Object getProperty(String name) {
        return this.source.get(name);
    }

    @Override
    public boolean containsProperty(String name) {
        return source.containsKey(name);
    }

}
