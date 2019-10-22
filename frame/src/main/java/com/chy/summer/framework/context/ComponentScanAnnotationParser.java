package com.chy.summer.framework.context;

import com.chy.summer.framework.beans.BeanUtils;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.support.BeanNameGenerator;
import com.chy.summer.framework.context.annotation.ClassPathBeanDefinitionScanner;
import com.chy.summer.framework.context.annotation.ScopeMetadataResolver;
import com.chy.summer.framework.context.annotation.constant.ScopedProxyMode;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.evn.Environment;
import com.chy.summer.framework.core.io.ResourceLoader;
import com.chy.summer.framework.core.type.classreading.MetadataReader;
import com.chy.summer.framework.core.type.classreading.MetadataReaderFactory;
import com.chy.summer.framework.core.type.filter.TypeFilter;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class ComponentScanAnnotationParser {


    private final BeanDefinitionRegistry registry
            ;
    private final Environment environment;
    private final BeanDefinitionRegistry resourceLoader;
    private final BeanNameGenerator beanNameGenerator;

    public ComponentScanAnnotationParser(Environment environment,
                                         ResourceLoader resourceLoader,
                                         BeanNameGenerator componentScanBeanNameGenerator,
                                         BeanDefinitionRegistry registry) {

        this.registry = registry;
        this.environment = environment;
        this.resourceLoader = registry;
        this.beanNameGenerator = componentScanBeanNameGenerator;

    }


    public Set<BeanDefinitionHolder> parse(AnnotationAttributes componentScan, final String declaringClass) throws IOException {
        //创建一个类路径解析器
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(this.registry,
                this.environment, this.resourceLoader);

        BeanNameGenerator beanNameGenerator = this.beanNameGenerator;
        //检查在 @ComponentScan 注解里有没有设置 name生成器
        Class<? extends BeanNameGenerator> generatorClass = componentScan
                .getRequiredAttribute("nameGenerator", Class.class);
        //如果等于 BeanNameGenerator 说明没有设置 ,如果设置了就去实例化这个 class
        if(BeanNameGenerator.class != generatorClass){
            beanNameGenerator = BeanUtils.instantiateClass(generatorClass);
        }
        scanner.setBeanNameGenerator(beanNameGenerator);

        //下面开始处理扫描路径
        //这是一个扫描路径的容器
        Set<String> basePackages = new LinkedHashSet<>();
        //这里先开始查找如果用户手动指定了扫描路径的
        String[] basePackagesArray = componentScan.getRequiredAttribute("basePackages",String[].class);
        for (String pkg : basePackagesArray) {
            basePackages.add(pkg);
        }
        //这里是手动指定了某个类为指定路径的,会把那个类的包路径当做扫描路径
        Class[] basePackagesClassArray = componentScan.getRequiredAttribute("basePackageClasses",Class[].class);
        for (Class<?> clazz : basePackagesClassArray) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }
        //如果上面都没有手动指定,就会把配置类的包路径当做扫描路径(在spring boot 就是这里指定main函数所在的类为扫描路径)
        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(declaringClass));
        }
        //添加一个排除器,把设置类给排除.这个设置类不会生成 bean 对象
        scanner.addExcludeFilter((metadataReader, metadataReaderFactory) -> {
            return declaringClass.equals(metadataReader.getClassMetadata().getClassName());
        });
        //开始扫描
        return scanner.scan(StringUtils.toStringArray(basePackages));
    }


}
