package com.chy.summer.framework.web.servlet;

import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.context.annotation.AnnotatedBeanDefinitionReader;
import com.chy.summer.framework.context.annotation.ClassPathBeanDefinitionScanner;
import com.chy.summer.framework.web.servlet.context.ServletWebServerApplicationContext;

import java.util.LinkedHashSet;
import java.util.Set;

public class AnnotationConfigServletWebServerApplicationContext extends ServletWebServerApplicationContext {

    private final AnnotatedBeanDefinitionReader reader;
    private final ClassPathBeanDefinitionScanner scanner;
    private final Set<Class<?>> annotatedClasses;
    private String[] basePackages;

    public AnnotationConfigServletWebServerApplicationContext() {
        this.annotatedClasses = new LinkedHashSet();
        this.reader = new AnnotatedBeanDefinitionReader(this);
        this.scanner = new ClassPathBeanDefinitionScanner(this);
    }

    public AnnotationConfigServletWebServerApplicationContext(String... basePackages) {
        this();
        this.scan(basePackages);
        this.refresh();
    }

    public final void scan(String... basePackages) {
        this.basePackages = basePackages;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        //开扫描对应路径下的类
        if (this.basePackages != null && this.basePackages.length > 0) {
           // this.scanner.scan(this.basePackages);
        }

//        if (!this.annotatedClasses.isEmpty()) {
//            this.reader.register(ClassUtils.toClassArray(this.annotatedClasses));
//        }
    }
}
