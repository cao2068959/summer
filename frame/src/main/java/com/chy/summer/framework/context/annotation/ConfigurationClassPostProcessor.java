package com.chy.summer.framework.context.annotation;

import com.chy.summer.framework.beans.config.*;
import com.chy.summer.framework.beans.support.BeanNameGenerator;
import com.chy.summer.framework.beans.support.DefaultBeanNameGenerator;
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

/**
 *
 *  解析入口类 然后扫描所有业务类
 *
 *  BeanDefinitionRegistryPostProcessor 的实现类,会在 refresh --> invokeBeanFactoryPostProcessors 的时候执行
 *
 * @see #postProcessBeanDefinitionRegistry(BeanDefinitionRegistry)  入口方法
 */
@Slf4j
public class ConfigurationClassPostProcessor implements BeanDefinitionRegistryPostProcessor,
        PriorityOrdered {

    private Environment environment;

    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    private MetadataReaderFactory metadataReaderFactory = new DefaultMetadataReaderFactory();

    private BeanNameGenerator componentScanBeanNameGenerator = new AnnotationBeanNameGenerator();

    private BeanNameGenerator importBeanNameGenerator = new DefaultBeanNameGenerator();


    //用了记录 registry 的hashcode ,对应的 registry已经执行过就会存入对应的 code,防止重复注册
    private final Set<Integer> registriesPostProcessed = new HashSet<>();

    private ConfigurationClassBeanDefinitionReader reader;

    /**
     * BeanDefinitionRegistryPostProcessor 处理器的执行方法,执行之前会先检查一下,防止多次执行
     * @param registry
     * @throws BeansException
     */
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
        //真正去解析入口类
        processConfigBeanDefinitions(registry);
    }


    /**
     * 会去ioc 拿所有已经注册进去的配置类,然后逐个解析
     * 在默认的boot 项目中,只有一个 入口类(main函数所在类) 作为配置类
     *
     * @param registry
     */
    public void processConfigBeanDefinitions(BeanDefinitionRegistry registry) {
        List<BeanDefinitionHolder> configCandidates = new ArrayList<>();
        String[] candidateNames = registry.getBeanDefinitionNames();

        //这里会扫描 所有的初始化的 bd,来判断是不是配置类,是的话就放入 configCandidates
        for (String beanName : candidateNames) {
            BeanDefinition beanDef = registry.getBeanDefinition(beanName);
            //检查 bd中的属性 CONFIGURATION_CLASS_ATTRIBUTE 里面的值是不是 full或者lite
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

        //这里就把所有的配置类给扫描了,然后放入 parser的configurationClasses对象里
        //这里如果扫描到的class有 @ComponentScan 注解,会扫描对应的目录,并且生成对应的bd放入 IOC容器里
        parser.parse(candidates);
        //这边又迭代了一次,去检查扫描到的类里面有没有  @Configuration 并且这个类是 final 类型的有的话报错,这里先忽略
        //parser.validate();
        //把上面的解析结果给全部转成 set 集合
        Set<ConfigurationClass> configClasses = new LinkedHashSet(parser.getConfigurationClasses().keySet());

        //初始化 configurationClass 解析器
        if (this.reader == null) {
            this.reader = new ConfigurationClassBeanDefinitionReader(registry, this.resourceLoader, this.environment,
                    this.importBeanNameGenerator);
        }
        //开始解析处理上面扫描出来的配置类, @bean注解 等配置的处理就在这里面
        this.reader.loadBeanDefinitions(configClasses);
        candidates.clear();
    }


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
