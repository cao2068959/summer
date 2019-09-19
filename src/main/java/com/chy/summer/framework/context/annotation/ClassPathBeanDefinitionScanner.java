package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.core.io.support.PathMatchingResourcePatternResolver;
import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.core.io.support.ResourcePatternResolver;
import com.chy.summer.framework.core.type.classreading.DefaultMetadataReaderFactory;
import com.chy.summer.framework.core.type.classreading.MetadataReader;
import com.chy.summer.framework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 定义bean的扫描器，检测指定包路径上的所有class，找出bean的候选类，
 * 使用指定的注册器注册相应的bean。
 * 默认扫描带有@component、@repository、@service或@controller的类
 */
public class ClassPathBeanDefinitionScanner {
    /**
     * 用来保存bean的定义
     */
    BeanDefinitionRegistry registry;
    /**
     * 用来保存资源文件的路径，提供带有*号通配符的资源路径
     */
    ResourcePatternResolver resourcePatternResolver;

    /**
     * 元数据读取器工厂，可以为每个原始资源创建用于缓存元数据的读取器
     */
    private DefaultMetadataReaderFactory metadataReaderFactory;

    /**
     * 根据定义对象创建ClassPathBeanDefinitionScanner
     *
     * @param registry
     */
    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        this.registry = registry;
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
    }


    public ResourcePatternResolver getResourcePatternResolver() {
        return resourcePatternResolver;
    }

    /**
     * 扫描 的总入口
     *
     * @param basePackages
     * @throws IOException
     */
    public void scan(String... basePackages) throws IOException {
        Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<BeanDefinitionHolder>();
        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidates = scanCandidateComponents(basePackage);

        }


    }


    /**
     * 扫描所有class文件然后用asm加载后 判断是否有注解,有的就放入 set中
     *
     * @param basePackage
     * @return
     * @throws IOException
     */
    public Set<BeanDefinition> scanCandidateComponents(String basePackage) throws IOException {
        //扫描class文件，全部放入resource中，这里的实例类是FileSystemResource
        //resources会保存文件的句柄d
        Resource[] resources = getResourcePatternResolver().getResources(basePackage);
        //拿到所有的class后用asm加载,判断是否有对应的注解,这里用 元数据处理器来解析
        for (Resource resource : resources) {
            MetadataReader metadataReader = getMetadataReaderFactory().getMetadataReader(resource);

        }

        return null;
    }


    public final MetadataReaderFactory getMetadataReaderFactory() {
        if (this.metadataReaderFactory == null) {
            this.metadataReaderFactory = new DefaultMetadataReaderFactory();
        }
        return this.metadataReaderFactory;
    }


    public static void main(String[] args) throws IOException {
        ClassPathBeanDefinitionScanner p = new ClassPathBeanDefinitionScanner(null);
        p.scanCandidateComponents("classpath*:com/chy");
    }


}
