package com.chy.summer.framework.core.evn.propertysource;

import com.chy.summer.framework.util.StringUtils;

import java.util.Collection;
import java.util.List;

public abstract class CommandLinePropertySource<T> extends PropertySource<T> {

    public static final String COMMAND_LINE_PROPERTY_SOURCE_NAME = "commandLineArgs";

    public CommandLinePropertySource(T source) {
        super(COMMAND_LINE_PROPERTY_SOURCE_NAME, source);
    }

    public CommandLinePropertySource(String name, T source) {
        super(name, source);
    }

    @Override
    public final String getProperty(String name) {

        Collection<String> optionValues = this.getOptionValues(name);
        if (optionValues == null) {
            return null;
        }
        else {
            return StringUtils.collectionToCommaDelimitedString(optionValues);
        }
    }


    protected abstract List<String> getOptionValues(String name);
}
