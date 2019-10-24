package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.config.AnnotatedBeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.support.AbstractBeanDefinition;
import com.chy.summer.framework.beans.support.BeanDefinitionReader;
import com.chy.summer.framework.beans.support.BeanNameGenerator;
import com.chy.summer.framework.context.ComponentScanAnnotationParser;
import com.chy.summer.framework.context.annotation.utils.AnnotationConfigUtils;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.evn.Environment;
import com.chy.summer.framework.core.io.ResourceLoader;
import com.chy.summer.framework.core.ordered.Ordered;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.core.type.ClassMetadata;
import com.chy.summer.framework.core.type.MethodMetadata;
import com.chy.summer.framework.core.type.StandardAnnotationMetadata;
import com.chy.summer.framework.core.type.classreading.MetadataReader;
import com.chy.summer.framework.core.type.classreading.MetadataReaderFactory;
import com.chy.summer.framework.exception.BeanDefinitionStoreException;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.ConfigurationClassUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

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


    /**
     *  解析 配置类
     * @param configClass
     * @throws Exception
     */
    protected void processConfigurationClass(ConfigurationClass configClass) throws Exception {

        ConfigurationClass existingClass = this.configurationClasses.get(configClass);
        if (existingClass != null) {
            return;
        }

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
            throws Exception {

        ClassMetadata metadata = sourceClass.getMetadata();
        if(!(metadata instanceof AnnotationMetadata)){
            return null;
        }
        // 拿到配置类上 所有注解
        AnnotationMetadata annotationMetadata = (AnnotationMetadata) metadata;
        //拿到注解 ComponentScan 上面的所有属性
        AnnotationAttributes componentScan = annotationMetadata.getAnnotationAttributes(ComponentScan.class);
        //根据 @ComponentScan 去扫描 对应的 class路径,生成 所有的 BeanDefinitionHolder
        Set<BeanDefinitionHolder> scannedBeanDefinitions =
                this.componentScanParser.parse(componentScan, sourceClass.getMetadata().getClassName());
        //TODO 上面一路走下来,只是解析了一个用户在入口指定的一个配置类,而那些 以bean的方式配置的 配置类,还没处理,所以这里还需要 迭代上面扫描出来的 BeanDefinition,递归处理配置类


        // @Import 注解的解析
        //先把 class 上面所有 @Import 注解里 value 里写的 class 都收集一下
        Set<SourceClass> imports = getImports(sourceClass);
        //开始处理 @Import 注解
        processImports(configClass, sourceClass, imports, true);

        // 然后处理 @Bean 注解
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

    private Set<MethodMetadata> retrieveBeanMethodMetadata(SourceClass sourceClass) {
        AnnotationMetadata annotationMetadata = sourceClass.getMetadata();
        Set<MethodMetadata> beanMethods = annotationMetadata.getAnnotatedMethods(Bean.class.getName());
        if (beanMethods.size() > 1 && original instanceof StandardAnnotationMetadata) {
            // Try reading the class file via ASM for deterministic declaration order...
            // Unfortunately, the JVM's standard reflection returns methods in arbitrary
            // order, even between different runs of the same application on the same JVM.
            try {
                AnnotationMetadata asm =
                        this.metadataReaderFactory.getMetadataReader(original.getClassName()).getAnnotationMetadata();
                Set<MethodMetadata> asmMethods = asm.getAnnotatedMethods(Bean.class.getName());
                if (asmMethods.size() >= beanMethods.size()) {
                    Set<MethodMetadata> selectedMethods = new LinkedHashSet<>(asmMethods.size());
                    for (MethodMetadata asmMethod : asmMethods) {
                        for (MethodMetadata beanMethod : beanMethods) {
                            if (beanMethod.getMethodName().equals(asmMethod.getMethodName())) {
                                selectedMethods.add(beanMethod);
                                break;
                            }
                        }
                    }
                    if (selectedMethods.size() == beanMethods.size()) {
                        // All reflection-detected methods found in ASM method set -> proceed
                        beanMethods = selectedMethods;
                    }
                }
            }
            catch (IOException ex) {
                logger.debug("Failed to read class file via ASM for determining @Bean method order", ex);
                // No worries, let's continue with the reflection metadata we started with...
            }
        }
        return beanMethods;
    }


    /**
     * 配置类上面 @Import 注解的 解析
     * @param configClass
     * @param currentSourceClass
     * @param importCandidates
     * @param checkForCircularImports
     */
    private void processImports(ConfigurationClass configClass, SourceClass currentSourceClass,
                                Collection<SourceClass> importCandidates, boolean checkForCircularImports) throws Exception {

        //如果没有 打上 @Import 注解就直接 跳过了
        if (importCandidates.isEmpty()) {
            return;
        }
        //循环把 @Import 上的类都给初始化了
        for (SourceClass candidate : importCandidates) {
            processConfigurationClass(candidate.asConfigClass(configClass));
        }
    }


    private Set<SourceClass> getImports(SourceClass sourceClass) throws IOException {
        Set<SourceClass> imports = new LinkedHashSet<>();
        Set<SourceClass> visited = new LinkedHashSet<>();
        collectImports(sourceClass, imports, visited);
        return imports;
    }

    private void collectImports(SourceClass sourceClass, Set<SourceClass> imports, Set<SourceClass> visited)
            throws IOException {

        if (visited.add(sourceClass)) {
            for (SourceClass annotation : sourceClass.getAnnotations()) {
                String annName = annotation.getMetadata().getClassName();
                if (!annName.startsWith("java") && !annName.equals(Import.class.getName())) {
                    collectImports(annotation, imports, visited);
                }
            }
            imports.addAll(sourceClass.getAnnotationImportAttributes());
        }
    }



    private class SourceClass implements Ordered {

        private final Object source;

        private final AnnotationMetadata metadata;

        public ConfigurationClass asConfigClass(ConfigurationClass importedBy) throws IOException {
            if (this.source instanceof Class) {
                return new ConfigurationClass((Class<?>) this.source, importedBy);
            }
            return new ConfigurationClass((MetadataReader) this.source, importedBy);
        }


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

        public Set<SourceClass> getAnnotations() throws IOException {
            Set<SourceClass> result = new LinkedHashSet<>();
            for (String className : this.metadata.getAnnotationTypes()) {
                try {
                    result.add(asSourceClass(className));
                }
                catch (Throwable ex) {

                }
            }
            return result;
        }

        public AnnotationMetadata getMetadata() {
            return metadata;
        }

        /**
         * 获取 @Import 注解上的 value值,并且把里面设置的 class 都转成 SourceClass
         * @return
         */
        public Set<SourceClass> getAnnotationImportAttributes()  {
            Set<SourceClass> result = new LinkedHashSet<>();
            AnnotationAttributes annotationAttributes = this.metadata.getAnnotationAttributes(Import.class);
            if (annotationAttributes == null) {
                return result;
            }
            Class[] values = annotationAttributes.getRequiredAttribute("value", Class[].class);

            if(values == null){
                return result;
            }

            for (Class clazz : values) {
                result.add(new SourceClass(clazz));
            }

            return result;

        }


        public Object getAnnotationAttributes(Class<? extends Annotation> annType, String attribute)  {
            AnnotationAttributes annotationAttributes = this.metadata.getAnnotationAttributes(annType);
            if (annotationAttributes == null) {
                return Collections.emptySet();
            }
            return annotationAttributes.getAttributeValue(attribute);

        }

    }


}
