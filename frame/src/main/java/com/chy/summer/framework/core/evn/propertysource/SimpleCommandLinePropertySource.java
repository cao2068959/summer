package com.chy.summer.framework.core.evn.propertysource;

import java.util.List;

public class SimpleCommandLinePropertySource extends CommandLinePropertySource<CommandLineArgs> {


    public SimpleCommandLinePropertySource(String... args) {
        super(CommandLineArgs.parse(args));
    }


    public SimpleCommandLinePropertySource(String name, String[] args) {
        super(name, CommandLineArgs.parse(args));
    }
    @Override
    protected List<String> getOptionValues(String name) {
        return this.source.getOptionValues(name);
    }

    @Override
    public boolean containsProperty(String name) {
        return source.containsOption(name);
    }
}
