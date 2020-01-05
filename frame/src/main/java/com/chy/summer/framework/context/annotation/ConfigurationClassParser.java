package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.BeanUtils;
import com.chy.summer.framework.beans.config.AnnotatedBeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.support.BeanNameGenerator;
import com.chy.summer.framework.context.ComponentScanAnnotationParser;
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
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.ConfigurationClassUtils;
import lombok.Getter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.chy.summer.framework.context.annotation.condition.ConfigurationCondition.ConfigurationPhase.PARSE_CONFIGURATION;

/**
 * 配置类 的解析器
 */
public class ConfigurationClassParser {


    private final MetadataReaderFactory metadataReaderFactory;


    private final Environment environment;

    private final ResourceLoader resourceLoader;

    private final BeanDefinitionRegistry registry;

    private final ComponentScanAnnotationParser componentScanParser;

    /**
     *  Condition 注解的解析器
     */
   // private final ConditionEvaluator conditionEvaluator;

    @Getter
    private final Map<ConfigurationClass, ConfigurationClass> configurationClasses = new LinkedHashMap<>();

    private final ImportStack importStack = new ImportStack();

    private final Map<String, ConfigurationClass> knownSuperclasses = new HashMap<>();




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
       // this.conditionEvaluator = new ConditionEvaluator();
    }


    /**
     * 开始解析配置类
     * @param configCandidates
     */
    public void parse(Set<BeanDefinitionHolder> configCandidates) {

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
                ex.printStackTrace();
                throw new BeanDefinitionStoreException("解析[%s]配置类失败,失败原因为: [%s] ",bd,ex.getMessage());
            }
        }
        // 也是对 @Import 的解析
        //processDeferredImportSelectors();
    }


    protected final void parse(AnnotationMetadata metadata, String beanName) throws Exception {
        processConfigurationClass(new ConfigurationClass(metadata, beanName));
    }

    protected final void parse(String className, String beanName) throws Exception {
        MetadataReader reader = this.metadataReaderFactory.getMetadataReader(className);
        processConfigurationClass(new ConfigurationClass(reader.getAnnotationMetadata(), beanName));
    }


    /**
     *  这里会循环的的模式去解析 配置文件  解析-> 返回父类 -> 父类继续解析 ->...-> 直到没有任何的父类(返回了Null)
     *
     *  这里会把所有的class对象转成一个 SourceClass 对象, 里面重点在于
     *  source: 目标类的类型
     *  AnnotationMetadata: 元数据类型的描述
     *
     * @param configClass
     * @throws Exception
     */
    protected void processConfigurationClass(ConfigurationClass configClass) throws Exception {

        //如果类上打了 @condition 注解，就去判断一下这个类是否应该被注入到容器里
        //if (this.conditionEvaluator.shouldSkip(configClass.getMetadata(), PARSE_CONFIGURATION)) {
       //     return;
       // }

        ConfigurationClass existingClass = this.configurationClasses.get(configClass);
        if (existingClass != null) {
            return;
        }

        // 用 configClass 生成  SourceClass
        SourceClass sourceClass = asSourceClass(configClass);
        do {
            //开始解析类,这里如果有返回值说明 这个类有对应的 父类什么要继续处理
            sourceClass = doProcessConfigurationClass(configClass, sourceClass);
        }
        while (sourceClass != null);

        this.configurationClasses.put(configClass, configClass);
    }


    /**
     * 解析配置文件的主流程
     * @param configClass 配置类 的描述对象
     * @param sourceClass 如果配置类有 继承什么那么 这个对象可能是父类等,如果没有任何继承关系,那么这个对象和上面的 configClass 相同
     * @return
     * @throws Exception
     */
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
        //打了@ComponentScan 注解才会去执行下面的扫描逻辑
        if(componentScan != null){
            //根据 @ComponentScan 去扫描 对应的 class路径,生成 所有的 BeanDefinitionHolder
            Set<BeanDefinitionHolder> scannedBeanDefinitions =
                    this.componentScanParser.parse(componentScan, sourceClass.getMetadata().getClassName());

            //在扫描了入口类之后发现了一堆类,这些类里面可能会存在配置类,需要循环处理
            for (BeanDefinitionHolder holder : scannedBeanDefinitions) {
                BeanDefinition beanDefinition = holder.getBeanDefinition();
                //检查是不是配置类 @Configuration @Component @ComponentScan @Import 或者 存在 @bean方法
                if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDefinition)) {
                    if(beanDefinition instanceof AnnotatedBeanDefinition){
                        AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
                        //把扫描到的类用递归 再走一次解析配置类的流程
                        parse(annotatedBeanDefinition.getMetadata(), holder.getBeanName());
                    }
                }
            }

        }

        // @Import 注解的解析
        //先把 class 上面所有 @Import 注解里 value 里写的 class 都收集一下
        Set<SourceClass> imports = getImports(sourceClass);
        //开始处理 @Import 注解
        processImports(configClass, sourceClass, imports, true);

        // 然后处理 @Bean 注解
        Set<MethodMetadata> beanMethods = retrieveBeanMethodMetadata(sourceClass);
        for (MethodMetadata methodMetadata : beanMethods) {
            //先把所有的 @bean 标注的方法存起来
            configClass.addBeanMethod(new BeanMethod(methodMetadata, configClass));
        }

        //去解析配置类上的父类,
        if (sourceClass.getMetadata().hasSuperClass()) {
            String superclass = sourceClass.getMetadata().getSuperClassName();
            if (superclass != null && !superclass.startsWith("java") &&
                    !this.knownSuperclasses.containsKey(superclass)) {
                this.knownSuperclasses.put(superclass, configClass);
                return sourceClass.getSuperClass();
            }
        }
        return null;
    }


    /**
     * 获取了 这个类下面 所有打了注解 @Bean 的方法
     * @param sourceClass
     * @return
     */
    private Set<MethodMetadata> retrieveBeanMethodMetadata(SourceClass sourceClass) {
        AnnotationMetadata annotationMetadata = sourceClass.getMetadata();
        //拿到了所有打了@Bean 的方法
        Set<MethodMetadata> beanMethods = annotationMetadata.getAnnotatedMethods(Bean.class.getName());
        if(beanMethods == null){
            return new HashSet<>();
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

        //防止循环依赖问题
        if(checkForCircularImports && isChainedImportOnStack(configClass)){
            return;
        }

        //开始执行之前先推入栈里
        this.importStack.push(configClass);

        try {
            //循环把 @Import 上的类都给初始化了
            for (SourceClass candidate : importCandidates) {
                //判断类是否实现了ImportBeanDefinitionRegistrar
                if (candidate.isAssignable(ImportBeanDefinitionRegistrar.class)){
                    Class<?> candidateClass = candidate.loadClass();
                    ImportBeanDefinitionRegistrar registrar =
                            BeanUtils.instantiateClass(candidateClass, ImportBeanDefinitionRegistrar.class);
                    ParserStrategyUtils.invokeAwareMethods(
                            registrar, this.environment, this.resourceLoader, this.registry);
                    configClass.addImportBeanDefinitionRegistrar(registrar, currentSourceClass.getMetadata());
                }else {
                    this.importStack.registerImport(candidate.getMetadata().getClassName(), currentSourceClass.getMetadata());
                    processConfigurationClass(candidate.asConfigClass(configClass));
                }
            }
        }finally {
            //执行结束后推出栈
            this.importStack.pop();
        }
    }

    /**
     * 检查 指定的类是不是已经在 栈里了,如果在说明这个类正在被执行.
     * @param configClass
     * @return
     */
    private boolean isChainedImportOnStack(ConfigurationClass configClass) {
        if (this.importStack.contains(configClass)) {
            return true;
        }
        return false;
    }


    private Set<SourceClass> getImports(SourceClass sourceClass) throws IOException {
        Set<SourceClass> imports = new LinkedHashSet<>();
        Set<SourceClass> visited = new LinkedHashSet<>();
        collectImports(sourceClass, imports, visited);
        return imports;
    }

    /**
     *
     * @param sourceClass 目标class
     * @param imports  结果放这里面,会把所有 @import 注解上涉及到的所有class放入这个容器
     * @param visited  用了防止重复扫描类的,处理过的class都会放入这个容器
     * @throws IOException
     */
    private void collectImports(SourceClass sourceClass, Set<SourceClass> imports, Set<SourceClass> visited)
            throws IOException {

        if (visited.add(sourceClass)) {
            //获取目标类上的所有的注解,然后遍历
            for (SourceClass annotation : sourceClass.getAnnotations()) {
                String annName = annotation.getMetadata().getClassName();
                //如果不是Java 自带的注解,并且不是 @Import 注解,那么就递归继续一层层扫描
                if (!annName.startsWith("java") && !annName.equals(Import.class.getName())) {
                    collectImports(annotation, imports, visited);
                }
            }
            //把sourceClass 对象所代表的class 上面所有@import注解所涉及到的所有class 放入容器,这里会包括处理父类上面的
            imports.addAll(sourceClass.getAnnotationImportAttributes());
        }
    }

    /**
     *  生成 SourceClass
     * @param configurationClass
     * @return
     * @throws Exception
     */
    private SourceClass asSourceClass(ConfigurationClass configurationClass) throws Exception {
        AnnotationMetadata metadata = configurationClass.getMetadata();
        if (metadata instanceof StandardAnnotationMetadata) {
            return new SourceClass(((StandardAnnotationMetadata) metadata).getIntrospectedClass());
        }

        return asSourceClass(metadata.getClassName());
    }

    SourceClass asSourceClass( String className) throws Exception {
        if (className == null) {
            return new SourceClass(Object.class);
        }
        return new SourceClass(this.metadataReaderFactory.getMetadataReader(className));
    }

    SourceClass asSourceClass(Class<?> classType) throws Exception {
        return new SourceClass(classType);
    }

//=================================下面是内部类==========================================================================

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
                    ex.printStackTrace();
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

        public SourceClass getSuperClass() throws Exception {
            if (this.source instanceof Class) {
                return asSourceClass(((Class<?>) this.source).getSuperclass());
            }
            return asSourceClass(((MetadataReader) this.source).getClassMetadata().getSuperClassName());
        }

        public boolean isAssignable(Class<?> clazz) {
            if (this.source instanceof Class) {
                return clazz.isAssignableFrom((Class<?>) this.source);
            }
            return false;
//            return new AssignableTypeFilter(clazz).match((MetadataReader) this.source, metadataReaderFactory);
        }

        public Class<?> loadClass() throws ClassNotFoundException {
            if (this.source instanceof Class) {
                return (Class<?>) this.source;
            }
            String className = ((MetadataReader) this.source).getClassMetadata().getClassName();
            return ClassUtils.forName(className, resourceLoader.getClassLoader());
        }
    }


    private static class ImportStack extends ArrayDeque<ConfigurationClass>  {

        private final Map<String, AnnotationMetadata> imports = new ConcurrentHashMap<>();

        public void registerImport(String importedClass , AnnotationMetadata importingClass) {
            this.imports.put(importedClass, importingClass);
        }

        public AnnotationMetadata getImportingClassFor(String importedClass) {
            return this.imports.get(importedClass);
        }

    }


}
