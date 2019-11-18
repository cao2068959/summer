package chy.test.service;

import com.chy.summer.framework.context.annotation.Bean;
import com.chy.summer.framework.context.annotation.Configuration;

@Configuration
public class BeanDemo {

    @Bean
    public BeanDemo2 beanDemo2(){
        return new BeanDemo2();
    }

}
