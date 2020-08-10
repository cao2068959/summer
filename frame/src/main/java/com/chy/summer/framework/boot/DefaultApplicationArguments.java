package com.chy.summer.framework.boot;

import java.util.List;
import java.util.Set;

public class DefaultApplicationArguments implements ApplicationArguments {



    private final String[] args;

    public DefaultApplicationArguments(String[] args) {
        this.args = args;
    }

    @Override
    public String[] getSourceArgs() {
        return new String[0];
    }

    @Override
    public Set<String> getOptionNames() {
        return null;
    }

    @Override
    public boolean containsOption(String name) {
        return false;
    }

    @Override
    public List<String> getOptionValues(String name) {
        return null;
    }

    @Override
    public List<String> getNonOptionArgs() {
        System.out.println(new Object());
        return null;
    }

}
