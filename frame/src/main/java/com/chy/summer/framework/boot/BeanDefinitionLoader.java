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
import lombok.Getter;
import lombok.Setter;

import java.util.Set;


/**
 *  BeanDefinition 的装载器, 这个 Loader 在原本的spring里有各种的 BeanDefinition 读取器 比如
 *  AnnotatedBeanDefinitionReader / XmlBeanDefinitionReader / ClassPathBeanDefinitionScanner
 *  让其能够从不同的 地方(XML, 注解, java配置) 中去加载 对应的 BeanDefinition
 *
 *  那么具体到底用什么类型的 reader 去加载 决定的关键在于 Set<Object> sources , 将会用 这个set容器中  source 的类型去路由到不同的reader中
 *
 *
 *  对于summer来说,只考虑 注解的形式去加载 BeanDefinition , 所以 只有 AnnotatedBeanDefinitionReader
 *  同时 sources 中的数据也仅仅只有 一个 class类型的数据, 这个class其实也就是  @SummerBootApplication 所在的那一个类
 *
 *
 */
public class BeanDefinitionLoader {

    private Set<Object> sources;

    private AnnotatedBeanDefinitionReader annotatedReader;

    @Getter
    @Setter
    private  ResourceLoader resourceLoader;


    BeanDefinitionLoader(BeanDefinitionRegistry registry, Set<Object> sources) {
        Assert.notNull(registry, "Registry must not be null");
        Assert.notEmpty(sources, "Sources must not be empty");
        this.sources = sources;
        this.annotatedReader = new AnnotatedBeanDefinitionReader(registry);
        //排除类
        //this.scanner.addExcludeFilter(new ClassExcludeFilter(sources));
    }


    /**
     *
     * @see #load(Class<?> source)  直接看这个方法进入解析
     * @return
     */
    public void load() {
        for (Object source : this.sources) {
            load(source);
        }
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
