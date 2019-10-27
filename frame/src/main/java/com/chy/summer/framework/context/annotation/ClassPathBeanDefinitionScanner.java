package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.BeanNameAware;
import com.chy.summer.framework.core.evn.Environment;
import com.chy.summer.framework.exception.BeanDefinitionCommonException;
import com.chy.summer.framework.annotation.stereotype.Component;
import com.chy.summer.framework.beans.config.AnnotatedBeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinition;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.support.BeanDefinitionReaderUtils;
import com.chy.summer.framework.beans.support.BeanNameGenerator;
import com.chy.summer.framework.context.annotation.utils.AnnotationConfigUtils;
import com.chy.summer.framework.core.io.support.PathMatchingResourcePatternResolver;
import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.core.io.support.ResourcePatternResolver;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.core.type.classreading.DefaultMetadataReaderFactory;
import com.chy.summer.framework.core.type.classreading.MetadataReader;
import com.chy.summer.framework.core.type.classreading.MetadataReaderFactory;
import com.chy.summer.framework.core.type.filter.AnnotationTypeFilter;
import com.chy.summer.framework.core.type.filter.TypeFilter;
import com.chy.summer.framework.util.ClassUtils;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 定义bean的扫描器，检测指定包路径上的所有class，找出bean的候选类，
 * 使用指定的注册器注册相应的bean。
 * 默认扫描带有@component、@repository、@service或@controller的类
 */
public class ClassPathBeanDefinitionScanner{

    private  Environment environment;

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
     * 用于判断 一个class是否是符合标准的类型,比如 是否是打了注解 @Component的
     */
    private final List<TypeFilter> includeFilters = new LinkedList<>();
    /**
     * 同上,只是是排除,这里先把 入口 xxApplication的类给排除了,因为上面注解数量比较多,比较影响效率
     */
    private final List<TypeFilter> excludeFilters = new LinkedList<>();

    /**
     * bean生命周期的判断器
     */
    private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();
    /**
     * 名字生成器
     */
    private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();
    /**
     * 根据定义对象创建ClassPathBeanDefinitionScanner
     *
     * @param registry
     */
    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        this.registry = registry;
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
        registerDefaultFilters();
    }

    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, Environment environment, BeanDefinitionRegistry resourceLoader) {
        this.registry = registry;
        this.environment = environment;
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
        registerDefaultFilters();

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
    public Set<BeanDefinitionHolder> scan(String... basePackages) throws IOException {
        Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<BeanDefinitionHolder>();
        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidates = scanCandidateComponents(basePackage);
            for (BeanDefinition definition : candidates) {
                //解析 这个类的作用域,其实就是解析一下 @Scope 注解 没写就默认是单例模式
                ScopeMetadata scopeMetadata = scopeMetadataResolver.resolveScopeMetadata(definition);
                definition.setScope(scopeMetadata.getScopeName());
                //解析用户有没有自己指定了 beanName 如：@Service("xxx")
                //如果没有指定就用默认的规则根据类名生成
                String beanName = this.beanNameGenerator.generateBeanName(definition, this.registry);
                //检查类上面是否还有一些其他影响类行为的注解把他对应设置的值放入BeanDefinition
                //比如 @Lazy @DependsOn 等
                if (definition instanceof AnnotatedBeanDefinition) {
                    AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) definition;
                    AnnotationConfigUtils.processCommonDefinitionAnnotations(annotatedBeanDefinition,
                            annotatedBeanDefinition.getMetadata());
                }
                //这里检查beanName 是否重名等其他的规则
                if(!checkCandidate(beanName,definition)){
                    continue;
                }
                BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(definition,beanName);
                // 如果在 @Scope 注解中设置了 代理模式，那么这里会把 beanDefinitionHolder 生成代理对象
                //如果没设置那么直接 返回传入进去的那个 beanDefinitionHolder
                beanDefinitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata,
                        beanDefinitionHolder, this.registry);
                beanDefinitions.add(beanDefinitionHolder);
                BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinitionHolder,this.registry);
            }
        }
        return beanDefinitions;
    }




    /**
     * 扫描所有class文件然后用asm加载后 判断是否有注解,有的就放入 set中
     *
     * @param basePackage
     * @return
     * @throws IOException
     */
    public Set<BeanDefinition> scanCandidateComponents(String basePackage) throws IOException {
        Set<BeanDefinition> result = new LinkedHashSet<>();
        //先把路径里面的 . 全部变成 /
        basePackage = ClassUtils.convertClassNameToResourcePath(basePackage);
        //加上 classpath*: 前缀
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +basePackage;
        //扫描class文件，全部放入resource中，这里的实例类是FileSystemResource
        //resources会保存文件的句柄d
        Resource[] resources = getResourcePatternResolver().getResources(packageSearchPath);
        //拿到所有的class后用asm加载,判断是否有对应的注解,这里用 元数据处理器来解析
        for (Resource resource : resources) {
            MetadataReader metadataReader = getMetadataReaderFactory().getMetadataReader(resource);
            //判断class 文件是否符合条件,比如是否有某个注解
            if(isCandidateComponent(metadataReader)){
                ScannedGenericBeanDefinition scannedGenericBeanDefinition = new ScannedGenericBeanDefinition(metadataReader);
                scannedGenericBeanDefinition.setResource(resource);
                if(isCandidateComponent(scannedGenericBeanDefinition)){
                    result.add(scannedGenericBeanDefinition);
                }
            }
        }
        return result;
    }


    public final MetadataReaderFactory getMetadataReaderFactory() {
        if (this.metadataReaderFactory == null) {
            this.metadataReaderFactory = new DefaultMetadataReaderFactory();
        }
        return this.metadataReaderFactory;
    }


    /**
     * 初始化默认的 类型过滤器
     */
    protected void registerDefaultFilters() {
        this.includeFilters.add(new AnnotationTypeFilter(Component.class));
    }

    protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException {
        for (TypeFilter tf : this.excludeFilters) {
            if (tf.match(metadataReader, getMetadataReaderFactory())) {
                return false;
            }
        }
        for (TypeFilter tf : this.includeFilters) {
            if (tf.match(metadataReader, getMetadataReaderFactory())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 把接口和抽象类排除
     *
     * @param beanDefinition
     * @return
     */
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return (metadata.isIndependent() &&
                (metadata.isConcrete() || (metadata.isAbstract() )));
    }

    /**
     * 检查一下 生成的 beanName 是否已经存在
     * @param beanName
     * @param beanDefinition
     * @return
     * @throws IllegalStateException
     */
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
        //在 BeanDefinition 容器中没有对应的 beanName 就直接放行
        BeanDefinition existingDef = this.registry.getBeanDefinition(beanName);
        if (existingDef == null) {
            return true;
        }
        //在原版spring中 BeanDefinition 会有包装类的形式要做对应的处理，而这里没有先忽略
        throw new BeanDefinitionCommonException("beanName：[%s] 已经存在 [%s] 和 [%s] 拥有了相同的beanName",
                beanName,beanDefinition,existingDef);
    }



    public static void main(String[] args) throws IOException {
        ClassPathBeanDefinitionScanner p = new ClassPathBeanDefinitionScanner(null);
        p.scan("classpath*:com/chy");
    }


    public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
        this.beanNameGenerator = beanNameGenerator;
    }

    public void addExcludeFilter(TypeFilter typeFilter) {
        excludeFilters.add(typeFilter);
    }


}
