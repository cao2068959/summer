package com.chy.summer.framework.core.evn.propertysource;

import java.util.Map;

public class PropertiesPropertySource extends MapPropertySource {

    public PropertiesPropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }

}
