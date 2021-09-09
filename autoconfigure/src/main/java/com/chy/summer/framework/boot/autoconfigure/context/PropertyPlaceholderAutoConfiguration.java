package com.chy.summer.framework.boot.autoconfigure.context;


import com.chy.summer.framework.context.annotation.Bean;
import com.chy.summer.framework.context.annotation.Configuration;
import com.chy.summer.framework.core.evn.resolver.PropertySourcesPlaceholderConfigurer;

@Configuration
public class PropertyPlaceholderAutoConfiguration {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
