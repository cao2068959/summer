package com.chy.summer.framework.boot.autoconfigure;

import java.util.Set;


public interface AutoConfigurationMetadata {


    boolean wasProcessed(String className);


    Integer getInteger(String className, String key);


    Integer getInteger(String className, String key, Integer defaultValue);


    Set<String> getSet(String className, String key);


    Set<String> getSet(String className, String key, Set<String> defaultValue);


    String get(String className, String key);


    String get(String className, String key, String defaultValue);

}
