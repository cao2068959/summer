package com.chy.summer.framework.boot.autoconfigure;


public interface AutoConfigurationImportFilter {

    boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata);

}
