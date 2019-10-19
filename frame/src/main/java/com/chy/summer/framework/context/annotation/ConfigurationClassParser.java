package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.config.AnnotatedBeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.support.AbstractBeanDefinition;
import com.chy.summer.framework.beans.support.BeanNameGenerator;
import com.chy.summer.framework.context.ComponentScanAnnotationParser;
import com.chy.summer.framework.context.annotation.utils.AnnotationConfigUtils;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.evn.Environment;
import com.chy.summer.framework.core.io.ResourceLoader;
import com.chy.summer.framework.core.ordered.Ordered;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.core.type.ClassMetadata;
import com.chy.summer.framework.core.type.StandardAnnotationMetadata;
import com.chy.summer.framework.core.type.classreading.MetadataReader;
import com.chy.summer.framework.core.type.classreading.MetadataReaderFactory;
import com.chy.summer.framework.exception.BeanDefinitionStoreException;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.ConfigurationClassUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * 配置类 的解析器
 */
public class ConfigurationClassParser {


    private final MetadataReaderFactory metadataReaderFactory;


    private final Environment environment;

    private final ResourceLoader resourceLoader;

    private final BeanDefinitionRegistry registry;

    private final ComponentScanAnnotationParser componentScanParser;

    private final Map<ConfigurationClass, ConfigurationClass> configurationClasses = new LinkedHashMap<>();



    public ConfigurationClassParser(MetadataReaderFactory metadataReaderFactory,
                                    Environment environment,
                                    ResourceLoader resourceLoader,
                                    BeanNameGenerator componentScanBeanNameGenerator,
                                    BeanDefinitionRegistry registry) {

        this.metadataReaderFactory = metadataReaderFactory;
        this.environment = environment;
        this.resourceLoader = resourceLoader;
        this.registry = registry;
        this.componentScanParser = new ComponentScanAnnotationParser(environment, resourceLoader, componentScanBeanNameGenerator, registry);
    }


    /**
     * 开始解析配置类
     * @param configCandidates
     */
    public void parse(Set<BeanDefinitionHolder> configCandidates) {
        this.deferredImportSelectors = new LinkedList<>();

        for (BeanDefinitionHolder holder : configCandidates) {
            BeanDefinition bd = holder.getBeanDefinition();
            try {
                if (bd instanceof AnnotatedBeanDefinition) {
                    parse(((AnnotatedBeanDefinition) bd).getMetadata(), holder.getBeanName());
                }
            }
            catch (BeanDefinitionStoreException ex) {
                throw ex;
            }
            catch (Throwable ex) {
                throw new BeanDefinitionStoreException("解析[%s]配置类失败,失败原因为: [%s] ",bd,ex.getMessage());
            }
        }

        processDeferredImportSelectors();
    }


    protected final void parse(AnnotationMetadata metadata, String beanName) throws Exception {
        processConfigurationClass(new ConfigurationClass(metadata, beanName));
    }

    protected void processConfigurationClass(ConfigurationClass configClass) throws Exception {

        // 用 configClass 生成  SourceClass
        SourceClass sourceClass = asSourceClass(configClass);
        do {
            //开始真正执行解析
            sourceClass = doProcessConfigurationClass(configClass, sourceClass);
        }
        while (sourceClass != null);

        this.configurationClasses.put(configClass, configClass);
    }


    /**
     *  生成 SourceClass
     * @param configurationClass
     * @return
     * @throws Exception
     */
    private SourceClass asSourceClass(ConfigurationClass configurationClass) throws Exception {
        AnnotationMetadata metadata = configurationClass.getMetadata();
        return asSourceClass(metadata.getClassName());
    }

    SourceClass asSourceClass( String className) throws Exception {
        if (className == null) {
            return new SourceClass(Object.class);
        }
        return new SourceClass(this.metadataReaderFactory.getMetadataReader(className));
    }



    protected final SourceClass doProcessConfigurationClass(ConfigurationClass configClass, SourceClass sourceClass)
            throws IOException {

        ClassMetadata metadata = sourceClass.getMetadata();
        if(!(metadata instanceof AnnotationMetadata)){
            return null;
        }
        // 拿到配置类上 所有
        AnnotationMetadata annotationMetadata = (AnnotationMetadata) metadata;
        //拿到注解 ComponentScan 上面的所有属性
        AnnotationAttributes componentScan = annotationMetadata.getAnnotationAttributes(ComponentScan.class);

        Set<BeanDefinitionHolder> scannedBeanDefinitions =
                this.componentScanParser.parse(componentScan, sourceClass.getMetadata().getClassName());
        // Check the set of scanned definitions for any further config classes and parse recursively if needed
        for (BeanDefinitionHolder holder : scannedBeanDefinitions) {
            BeanDefinition bdCand = holder.getBeanDefinition().getOriginatingBeanDefinition();
            if (bdCand == null) {
                bdCand = holder.getBeanDefinition();
            }
            if (ConfigurationClassUtils.checkConfigurationClassCandidate(bdCand, this.metadataReaderFactory)) {
                parse(bdCand.getBeanClassName(), holder.getBeanName());
            }
        }

        // Process any @Import annotations
        processImports(configClass, sourceClass, getImports(sourceClass), true);

        // Process any @ImportResource annotations
        AnnotationAttributes importResource =
                AnnotationConfigUtils.attributesFor(sourceClass.getMetadata(), ImportResource.class);
        if (importResource != null) {
            String[] resources = importResource.getStringArray("locations");
            Class<? extends BeanDefinitionReader> readerClass = importResource.getClass("reader");
            for (String resource : resources) {
                String resolvedResource = this.environment.resolveRequiredPlaceholders(resource);
                configClass.addImportedResource(resolvedResource, readerClass);
            }
        }

        // Process individual @Bean methods
        Set<MethodMetadata> beanMethods = retrieveBeanMethodMetadata(sourceClass);
        for (MethodMetadata methodMetadata : beanMethods) {
            configClass.addBeanMethod(new BeanMethod(methodMetadata, configClass));
        }

        // Process default methods on interfaces
        processInterfaces(configClass, sourceClass);

        // Process superclass, if any
        if (sourceClass.getMetadata().hasSuperClass()) {
            String superclass = sourceClass.getMetadata().getSuperClassName();
            if (superclass != null && !superclass.startsWith("java") &&
                    !this.knownSuperclasses.containsKey(superclass)) {
                this.knownSuperclasses.put(superclass, configClass);
                // Superclass found, return its annotation metadata and recurse
                return sourceClass.getSuperClass();
            }
        }

        // No superclass -> processing is complete
        return null;
    }


    private class SourceClass implements Ordered {

        private final Object source;

        private final AnnotationMetadata metadata;

        public SourceClass(Object source) {
            this.source = source;
            if (source instanceof Class) {
                this.metadata = new StandardAnnotationMetadata((Class<?>) source, true);
            } else {
                this.metadata = ((MetadataReader) source).getAnnotationMetadata();
            }
        }

        @Override
        public int getOrder() {
            Integer order = ConfigurationClassUtils.getOrder(this.metadata);
            return (order != null ? order : Ordered.LOWEST_PRECEDENCE);
        }

        public ClassMetadata getMetadata() {
            return metadata;
        }
    }


    }
