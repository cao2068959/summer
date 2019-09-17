package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class ClassPathBeanDefinitionScanner {

    BeanDefinitionRegistry registry;

    public static final String CLASSPATH_ALL_URL_PREFIX = "classpath*:";


    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }



    public void scan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<BeanDefinitionHolder>();
        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidates = scanCandidateComponents(basePackage);

        }


    }



    public Set<BeanDefinition> scanCandidateComponents(String basePackage) {

    }


}
