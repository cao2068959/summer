package com.chy.summer.framework.autoconfigure.context;

import com.chy.summer.framework.context.annotation.Bean;
import com.chy.summer.framework.context.annotation.Configuration;
import com.chy.summer.framework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
public class PropertyPlaceholderAutoConfiguration {

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
