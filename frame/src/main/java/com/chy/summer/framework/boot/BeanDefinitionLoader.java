package com.chy.summer.framework.boot;

import com.chy.summer.framework.annotation.stereotype.Component;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.support.BeanDefinitionReader;
import com.chy.summer.framework.context.annotation.AnnotatedBeanDefinitionReader;
import com.chy.summer.framework.context.annotation.ClassPathBeanDefinitionScanner;
import com.chy.summer.framework.core.annotation.AnnotationUtils;
import com.chy.summer.framework.core.evn.ConfigurableEnvironment;
import com.chy.summer.framework.core.io.ResourceLoader;
import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.util.Assert;

import java.util.Set;

public class BeanDefinitionLoader {

    private Set<Object> sources;

    private AnnotatedBeanDefinitionReader annotatedReader;

    private BeanDefinitionReader groovyReader;

    private ClassPathBeanDefinitionScanner scanner;

    private ResourceLoader resourceLoader;


    BeanDefinitionLoader(BeanDefinitionRegistry registry, Set<Object> sources) {
        Assert.notNull(registry, "Registry must not be null");
        Assert.notEmpty(sources, "Sources must not be empty");
        this.sources = sources;
        this.annotatedReader = new AnnotatedBeanDefinitionReader(registry);
        this.scanner = new ClassPathBeanDefinitionScanner(registry);
        //排除类
        //this.scanner.addExcludeFilter(new ClassExcludeFilter(sources));
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }


    /**
     *
     * @see #load(Class<?> source)  直接看这个方法进入解析
     * @return
     */
    public int load() {
        int count = 0;
        for (Object source : this.sources) {
            count += load(source);
        }
        return count;
    }

    private int load(Object source) {
        Assert.notNull(source, "Source must not be null");
        if (source instanceof Class<?>) {
            return load((Class<?>) source);
        }
        throw new IllegalArgumentException("Invalid source type " + source.getClass());
    }


    /**
     * 开始解析 入口类,这里用的 annotatedReader 是 AnnotatedBeanDefinitionReader
     *
     * @param source
     * @return
     */
    private int load(Class<?> source) {
        if (isComponent(source)) {
            this.annotatedReader.register(source);
            return 1;
        }
        return 0;
    }


    /**
     * 判断这个类上面有没有 @Component 注解
     * @param type
     * @return
     */
    private boolean isComponent(Class<?> type) {
        if (AnnotationUtils.findAnnotation(type, Component.class) != null) {
            return true;
        }
        return false;
    }


}
