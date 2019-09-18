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

public class ClassPathBeanDefinitionScanner {

    BeanDefinitionRegistry registry;

    ResourcePatternResolver resourcePatternResolver;
    private DefaultMetadataReaderFactory metadataReaderFactory;


    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        this.registry = registry;
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
    }


    public ResourcePatternResolver getResourcePatternResolver() {
        return resourcePatternResolver;
    }

    /**
     * 扫描 的总入口
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
