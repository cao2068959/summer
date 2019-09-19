package com.chy.summer.framework.web.servlet;

import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.context.annotation.AnnotatedBeanDefinitionReader;
import com.chy.summer.framework.context.annotation.ClassPathBeanDefinitionScanner;
import com.chy.summer.framework.web.servlet.context.ServletWebServerApplicationContext;

import java.util.LinkedHashSet;
import java.util.Set;

public class AnnotationConfigServletWebServerApplicationContext extends ServletWebServerApplicationContext {
    /**
     * 注释bean的定义注册器，一种替代ClassPathBeanDefinitionScanner的方法
     */
    private final AnnotatedBeanDefinitionReader reader;
    /**
     * 注解bean注册扫描器，默认扫描@Component， @Repository， @Service，或 @Controller
     */
    private final ClassPathBeanDefinitionScanner scanner;
    /**
     * 需要扫描的注解
     */
    private final Set<Class<?>> annotatedClasses;
    /**
     * 需要扫描的路径
     */
    private String[] basePackages;

    /**
     * 初始化
     */
    public AnnotationConfigServletWebServerApplicationContext() {
        this.annotatedClasses = new LinkedHashSet();
        this.reader = new AnnotatedBeanDefinitionReader(this);
        this.scanner = new ClassPathBeanDefinitionScanner(this);
    }

    /**
     * 初始化并指定扫描路径，之后开始执行扫描
     * @param basePackages 需要扫描的路径
     */
    public AnnotationConfigServletWebServerApplicationContext(String... basePackages) {
        this();
        this.scan(basePackages);
        this.refresh();
    }

    /**
     * 配置扫描路径
     * @param basePackages 需要扫描的路径
     */
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
