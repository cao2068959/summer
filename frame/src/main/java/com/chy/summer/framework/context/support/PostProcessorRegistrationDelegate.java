package com.chy.summer.framework.context.support;


import com.chy.summer.framework.beans.config.*;
import com.chy.summer.framework.beans.support.DefaultListableBeanFactory;
import com.chy.summer.framework.beans.support.MergedBeanDefinitionPostProcessor;
import com.chy.summer.framework.core.PriorityOrdered;
import com.chy.summer.framework.core.ordered.OrderComparator;
import com.chy.summer.framework.core.ordered.Ordered;

import java.util.*;

public class PostProcessorRegistrationDelegate {

    /**
     * BeanFactory 后置处理器的 执行器
     * @param beanFactory
     * @param beanFactoryPostProcessors
     */
    public static void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory,
                                                       List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {
        Set<String> processedBeans = new HashSet<>();
        List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();

        if (beanFactory instanceof BeanDefinitionRegistry) {
            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
            List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();
            //这里把 postProcessor 分成2类 放入2个不同的容器里,主要还是对入参的一个处理
            //入参进来的后置处理器也是优先级最高的
            for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
                if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
                    BeanDefinitionRegistryPostProcessor registryProcessor = (BeanDefinitionRegistryPostProcessor) postProcessor;
                    registryProcessor.postProcessBeanDefinitionRegistry(registry);
                    registryProcessors.add(registryProcessor);
                }
                else {
                    regularPostProcessors.add(postProcessor);
                }
            }
            //开始处理 BeanDefinitionRegistryPostProcessor 类型的后置处理器
            beanDefinitionRegistryPostProcessorHandle(beanFactory,registry,registryProcessors,processedBeans);

        }else{
            //TODO 暂时没有这个情况,先不考虑
        }

        //只处理 BeanFactoryPostProcessor 类型的后置处理器
        beanFactoryPostProcessorHandle(beanFactory,processedBeans,regularPostProcessors);

        //TODO 这边暂时不清理
        //beanFactory.clearMetadataCache();
    }


    private static void beanFactoryPostProcessorHandle(ConfigurableListableBeanFactory beanFactory,
                                                Set<String> processedBeans,
                                                List<BeanFactoryPostProcessor> regularPostProcessors){

        //执行只带有BeanFactoryPostProcessor 接口的 BeanFactoryPostProcessor的方法
        //这里主要处理的还是入参时候带来的 BeanFactoryPostProcessor 后置处理器
        invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);

        //和上面类似,只是这里那的是 BeanFactoryPostProcessor 类型的后置处理器
        String[] postProcessorNames =
                beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

        List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
        List<String> orderedPostProcessorNames = new ArrayList<>();
        List<String> nonOrderedPostProcessorNames = new ArrayList<>();
        for (String ppName : postProcessorNames) {
            if (processedBeans.contains(ppName)) {
                //已经被执行过了,直接跳过
            }
            else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
                priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
            }
            else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
                orderedPostProcessorNames.add(ppName);
            }
            else {
                nonOrderedPostProcessorNames.add(ppName);
            }
        }

        //排序
        sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
        //执行 被 priorityOrdered 接口/注解 标注过的处理器
        invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

        //这里排序执行 被 ordered 接口/注解 标注过的处理器
        List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>();
        for (String postProcessorName : orderedPostProcessorNames) {
            orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
        }
        sortPostProcessors(orderedPostProcessors, beanFactory);
        invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

        //这里排序执行 什么都没有的屌丝处理器
        List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
        for (String postProcessorName : nonOrderedPostProcessorNames) {
            nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
        }
        invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

    }


    /**
     * 这里是专门处理 beanDefinitionRegistryPostProcessor 类型的后置处理器的
     * @param beanFactory
     * @param registry
     * @param registryProcessors
     * @param processedBeans
     */
    private static void beanDefinitionRegistryPostProcessorHandle(ConfigurableListableBeanFactory beanFactory,
                                                           BeanDefinitionRegistry registry,
                                                           List<BeanDefinitionRegistryPostProcessor> registryProcessors,
                                                           Set<String> processedBeans){
        //拿到所有已经在 beanFactory 里BeanDefinitionRegistryPostProcessor后置处理器的bean的名字
        String[] postProcessorNames =
                beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);

        //先执行 继承了 PriorityOrdered 接口的后置处理器
        doBeanDefinitionRegistryPostProcessor(beanFactory,registry,processedBeans
                ,registryProcessors,postProcessorNames, (postprocessorName)->{
                    return beanFactory.isTypeMatch(postprocessorName, PriorityOrdered.class);
                });

        //然后执行了 继承了 Ordered 接口的后置处理器
        doBeanDefinitionRegistryPostProcessor(beanFactory,registry,processedBeans
                ,registryProcessors,postProcessorNames,(postprocessorName)->{
                    return !processedBeans.contains(postprocessorName) && beanFactory.isTypeMatch(postprocessorName, Ordered.class);
                });

        //最后执行剩下的所有 后置处理器
        doBeanDefinitionRegistryPostProcessor(beanFactory,registry,processedBeans
                ,registryProcessors,postProcessorNames,(postprocessorName)->{
                    return !processedBeans.contains(postprocessorName);
                });

        //执行带有BeanDefinitionRegistryPostProcessor 并且带有 BeanFactoryPostProcessor 接口的处理器 的BeanFactoryPostProcessor方法
        invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
    }


    /**
     *  用来执行  BeanDefinitionRegistryPostProcessor 处理器
     * @param beanFactory
     * @param registry
     * @param processedBeans  处理过的 PostProcessor 都会放入这里面,避免重复执行
     * @param postProcessorNames  要处理的postProcessor 的名字
     */
    private static void  doBeanDefinitionRegistryPostProcessor(ConfigurableListableBeanFactory beanFactory,
                                                               BeanDefinitionRegistry registry,
                                                               Set<String> processedBeans ,
                                                               List<BeanDefinitionRegistryPostProcessor> registryProcessors ,
                                                               String[] postProcessorNames ,
                                                               CustomPostProcessorRule customPostProcessorRule){


        List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new LinkedList<>();

        for (String ppName : postProcessorNames) {
            //如果 对应的后置处理器继承了 PriorityOrdered 接口 会将排序,然后处理
            //这里会调用getBean 对应的处理器bean会在这里初始化
            if (customPostProcessorRule.rule(ppName)) {
                currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
                processedBeans.add(ppName);
            }
        }
        //排序
        sortPostProcessors(currentRegistryProcessors, beanFactory);
        //把刚刚循环过的那些后置处理器给 塞入统一的容器里
        registryProcessors.addAll(currentRegistryProcessors);
        //执行后置处理器
        invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);

    }

    /**
     * 执行 BeanDefinitionRegistryPostProcessor 的 后置处理器,就是调用了 postProcessBeanDefinitionRegistry() 方法
     */
    private static void invokeBeanDefinitionRegistryPostProcessors(
            Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {

        for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
            postProcessor.postProcessBeanDefinitionRegistry(registry);
        }
    }

    /**
     * 执行 BeanFactoryPostProcessor 的 后置处理器,就是调用了 postProcessBeanFactory() 方法
     */
    private static void invokeBeanFactoryPostProcessors(
            Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

        for (BeanFactoryPostProcessor postProcessor : postProcessors) {
            postProcessor.postProcessBeanFactory(beanFactory);
        }
    }


    /**
     * 排序
     * @param postProcessors
     * @param beanFactory
     */
    private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
        Comparator<Object> comparatorToUse = null;
        if (beanFactory instanceof DefaultListableBeanFactory) {
            comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
        }
        if (comparatorToUse == null) {
            comparatorToUse = OrderComparator.INSTANCE;
        }
        postProcessors.sort(comparatorToUse);
    }


    /**
     * 用于注册 bean 的后置处理器
     * @param beanFactory
     * @param applicationContext
     */
    public static void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory,
                                                  AbstractApplicationContext applicationContext) {

        //找到 所有实现了 BeanPostProcessor 接口的 beanName
        String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

        //实现了 priorityOrdered 接口的
        List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
        List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
        //实现了 ordered 接口的
        List<String> orderedPostProcessorNames = new ArrayList<>();
        //什么都没实现的
        List<String> nonOrderedPostProcessorNames = new ArrayList<>();
        for (String ppName : postProcessorNames) {
            if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
                BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
                priorityOrderedPostProcessors.add(pp);
                if (pp instanceof MergedBeanDefinitionPostProcessor) {
                    internalPostProcessors.add(pp);
                }
            }
            else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
                orderedPostProcessorNames.add(ppName);
            }
            else {
                nonOrderedPostProcessorNames.add(ppName);
            }
        }

        //  排序priorityOrdered 的然后 注册进去
        sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
        registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);


        List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
        for (String ppName : orderedPostProcessorNames) {
            BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
            orderedPostProcessors.add(pp);
            if (pp instanceof MergedBeanDefinitionPostProcessor) {
                internalPostProcessors.add(pp);
            }
        }

        //  排序ordered 的然后 注册进去
        sortPostProcessors(orderedPostProcessors, beanFactory);
        registerBeanPostProcessors(beanFactory, orderedPostProcessors);


        List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
        for (String ppName : nonOrderedPostProcessorNames) {
            BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
            nonOrderedPostProcessors.add(pp);
            if (pp instanceof MergedBeanDefinitionPostProcessor) {
                internalPostProcessors.add(pp);
            }
        }

        //屌丝什么都没的注册
        registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);
        sortPostProcessors(internalPostProcessors, beanFactory);
        registerBeanPostProcessors(beanFactory, internalPostProcessors);
    }


    /**
     * 把 BeanPostProcessors 给放入 队列
     * @param beanFactory
     * @param postProcessors
     */
    private static void registerBeanPostProcessors(
            ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

        for (BeanPostProcessor postProcessor : postProcessors) {
            beanFactory.addBeanPostProcessor(postProcessor);
        }
    }



    /**
     * 用于自定义规则
     */
    private  interface CustomPostProcessorRule{

        boolean rule(String postprocessorName);

    }



}
