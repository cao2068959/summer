package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.core.io.support.PathMatchingResourcePatternResolver;
import com.chy.summer.framework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class ClassPathBeanDefinitionScanner {

    BeanDefinitionRegistry registry;

    ResourcePatternResolver resourcePatternResolver;


    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        this.registry = registry;
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
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
