package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.config.*;
import com.chy.summer.framework.beans.support.BeanNameGenerator;
import com.chy.summer.framework.core.PriorityOrdered;
import com.chy.summer.framework.core.evn.Environment;
import com.chy.summer.framework.core.io.DefaultResourceLoader;
import com.chy.summer.framework.core.io.ResourceLoader;
import com.chy.summer.framework.core.type.classreading.DefaultMetadataReaderFactory;
import com.chy.summer.framework.core.type.classreading.MetadataReaderFactory;
import com.chy.summer.framework.exception.BeansException;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.ConfigurationClassUtils;
import com.chy.summer.framework.web.servlet.context.support.StandardServletEnvironment;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class ConfigurationClassPostProcessor implements BeanDefinitionRegistryPostProcessor,
        PriorityOrdered {

    private Environment environment;


    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    private MetadataReaderFactory metadataReaderFactory = new DefaultMetadataReaderFactory();

    private BeanNameGenerator componentScanBeanNameGenerator = new AnnotationBeanNameGenerator();

    private BeanNameGenerator importBeanNameGenerator = new AnnotationBeanNameGenerator() {
        @Override
        protected String buildDefaultBeanName(BeanDefinition definition) {
            String beanClassName = definition.getBeanClassName();
            Assert.state(beanClassName != null, "No bean class name set");
            return beanClassName;
        }
    };


    //用了记录 registry 的hashcode ,对应的 registry已经执行过就会存入对应的 code,防止重复注册
    private final Set<Integer> registriesPostProcessed = new HashSet<>();

    private ConfigurationClassBeanDefinitionReader reader;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        //获取了 registry 的hashcode,用来做唯一表示
        int registryId = System.identityHashCode(registry);
        //如果对应的 registry 已经被执行过了,那么直接放弃操作
        if(registriesPostProcessed.contains(registryId)){
            throw new IllegalStateException(
                    "postProcessBeanDefinitionRegistry 已经重复执行过了 : " + registry);
        }

        this.registriesPostProcessed.add(registryId);
        processConfigBeanDefinitions(registry);
    }



    public void processConfigBeanDefinitions(BeanDefinitionRegistry registry) {
        List<BeanDefinitionHolder> configCandidates = new ArrayList<>();
        String[] candidateNames = registry.getBeanDefinitionNames();

        //这里会扫描 所有的初始化的 bd,来判断是不是配置类,是的话就放入 configCandidates
        for (String beanName : candidateNames) {
            BeanDefinition beanDef = registry.getBeanDefinition(beanName);
            //如果 bd 已经被检验过一次那么就会设置 Full 和 Lite 标志这里检查就是为了查重
            if (ConfigurationClassUtils.isFullConfigurationClass(beanDef) ||
                    ConfigurationClassUtils.isLiteConfigurationClass(beanDef)) {
                log.info(" bean [{}] 已经被验证为配置类了,请不要重复验证",beanName);

            //这里就去检查一下 这个 beanDefine 是不是配置类,如果是返回True 然后在 bd 里设置 Full 和 Lite 标志
            } else if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDef)) {
                configCandidates.add(new BeanDefinitionHolder(beanDef, beanName));
            }
        }

        // 如果没配置类 就直接结束了
        if (configCandidates.isEmpty()) {
            return;
        }

        // 根据 @Order 排序
        configCandidates.sort((bd1, bd2) -> {
            int i1 = ConfigurationClassUtils.getOrder(bd1.getBeanDefinition());
            int i2 = ConfigurationClassUtils.getOrder(bd2.getBeanDefinition());
            return Integer.compare(i1, i2);
        });

        //
        SingletonBeanRegistry sbr = null;
        if (registry instanceof SingletonBeanRegistry) {
            sbr = (SingletonBeanRegistry) registry;
        }

        if (this.environment == null) {
            this.environment = new StandardServletEnvironment();
        }

        // 生成 配置类的解析器
        ConfigurationClassParser parser = new ConfigurationClassParser(this.metadataReaderFactory, this.environment,
                this.resourceLoader, this.componentScanBeanNameGenerator, registry);

        Set<BeanDefinitionHolder> candidates = new LinkedHashSet<>(configCandidates);
        Set<ConfigurationClass> alreadyParsed = new HashSet<>(configCandidates.size());

        //这`里就把所有的class给扫描了,然后放入 parser的configurationClasses对象里
        parser.parse(candidates);
        //这边又迭代了一次,去检查扫描到的类里面有没有  @Configuration 并且这个类是 final 类型的有的话报错,这里先忽略
        //parser.validate();
        Set<ConfigurationClass> configClasses = new LinkedHashSet(parser.getConfigurationClasses().keySet());
        configClasses.removeAll(alreadyParsed);

        //初始化 configurationClass 解析器
        if (this.reader == null) {
            this.reader = new ConfigurationClassBeanDefinitionReader(
                    registry, this.resourceLoader, this.environment,
                    this.importBeanNameGenerator);
        }
        this.reader.loadBeanDefinitions(configClasses);
        alreadyParsed.addAll(configClasses);
        candidates.clear();
    }


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("执行了 postProcessBeanFactory");
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
