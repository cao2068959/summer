package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.core.io.support.PathMatchingResourcePatternResolver;
import com.chy.summer.framework.core.io.support.Resource;
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


    public ResourcePatternResolver getResourcePatternResolver() {
        return resourcePatternResolver;
    }


    public void scan(String... basePackages) throws IOException {
        Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<BeanDefinitionHolder>();
        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidates = scanCandidateComponents(basePackage);

        }


    }



    public Set<BeanDefinition> scanCandidateComponents(String basePackage) throws IOException {
        //扫描class文件，全部放入resource中，这里的实例类是FileSystemResource
        //resources会保存文件的句柄d
        Resource[] resources = getResourcePatternResolver().getResources(basePackage);

        return null;
    }


}
