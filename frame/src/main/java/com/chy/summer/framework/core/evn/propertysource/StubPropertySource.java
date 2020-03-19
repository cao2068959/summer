package com.chy.summer.framework.core.evn.propertysource;

public class StubPropertySource extends PropertySource<Object> {

    public StubPropertySource(String name) {
        super(name, new Object());
    }

    @Override
    public Object getProperty(String name) {
        return null;
    }
}
