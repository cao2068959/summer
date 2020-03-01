package com.chy.summer.framework.core.evn;

import com.chy.summer.framework.core.evn.propertysource.PropertySource;
import com.chy.summer.framework.core.io.support.Resource;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PropertiesPropertySourceLoader implements PropertySourceLoader {
    @Override
    public String[] getFileExtensions() {
        return new String[]{"properties"};
    }

    @Override
    public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
        Map<String, ?> properties = loadProperties(resource);
        if (properties.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new OriginTrackedMapPropertySource(name, Collections.unmodifiableMap(properties), true));
    }

    private Map<String, ?> loadProperties(Resource resource) throws IOException {
        return new OriginTrackedPropertiesLoader(resource).load(true);
    }
}
