package chy.test.service;

import com.chy.summer.framework.context.annotation.Bean;
import com.chy.summer.framework.context.annotation.Configuration;

@Configuration
public class BeanDemo {

    public BeanDemo(Abc abc){
        System.out.println(abc);
    }

    @Bean
    public BeanDemo2 beanDemo2(Abc2 abc2){
        BeanDemo2 beanDemo2 = new BeanDemo2();
        beanDemo2.setName("小明");
        return beanDemo2;
    }

}
