package com.chy.summer.framework.core.evn.propertysource;

import java.util.*;

public class CommandLineArgs {

    private final Map<String, List<String>> optionArgs = new HashMap<>();
    private final List<String> nonOptionArgs = new ArrayList<>();


    public void addOptionArg(String optionName,  String optionValue) {
        if (!this.optionArgs.containsKey(optionName)) {
            this.optionArgs.put(optionName, new ArrayList<>());
        }
        if (optionValue != null) {
            this.optionArgs.get(optionName).add(optionValue);
        }
    }


    public Set<String> getOptionNames() {
        return Collections.unmodifiableSet(this.optionArgs.keySet());
    }


    public boolean containsOption(String optionName) {
        return this.optionArgs.containsKey(optionName);
    }


    public List<String> getOptionValues(String optionName) {
        return this.optionArgs.get(optionName);
    }


    public void addNonOptionArg(String value) {
        this.nonOptionArgs.add(value);
    }


    public List<String> getNonOptionArgs() {
        return Collections.unmodifiableList(this.nonOptionArgs);
    }


    /**
     * 用了解析 命令行参数的
     * @param args
     * @return
     */
    public static CommandLineArgs parse(String... args) {
        CommandLineArgs commandLineArgs = new CommandLineArgs();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String optionText = arg.substring(2, arg.length());
                String optionName;
                String optionValue = null;
                if (optionText.contains("=")) {
                    optionName = optionText.substring(0, optionText.indexOf('='));
                    optionValue = optionText.substring(optionText.indexOf('=')+1, optionText.length());
                }
                else {
                    optionName = optionText;
                }
                if (optionName.isEmpty() || (optionValue != null && optionValue.isEmpty())) {
                    throw new IllegalArgumentException("Invalid argument syntax: " + arg);
                }
                commandLineArgs.addOptionArg(optionName, optionValue);
            }
            else {
                commandLineArgs.addNonOptionArg(arg);
            }
        }
        return commandLineArgs;
    }
}
