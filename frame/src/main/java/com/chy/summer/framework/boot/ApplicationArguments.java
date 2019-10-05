package com.chy.summer.framework.boot;

import java.util.List;
import java.util.Set;

public interface ApplicationArguments {


    String[] getSourceArgs();


    Set<String> getOptionNames();


    boolean containsOption(String name);


    List<String> getOptionValues(String name);


    List<String> getNonOptionArgs();

}
