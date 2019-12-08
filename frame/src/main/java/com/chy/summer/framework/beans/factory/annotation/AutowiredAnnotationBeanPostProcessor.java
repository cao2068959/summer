package com.chy.summer.framework.beans.factory.annotation;


import com.chy.summer.framework.annotation.beans.Autowired;
import com.chy.summer.framework.annotation.beans.Value;
import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.BeanFactoryAware;
import com.chy.summer.framework.beans.BeanUtils;
import com.chy.summer.framework.beans.PropertyValues;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.beans.config.SmartInstantiationAwareBeanPostProcessor;
import com.chy.summer.framework.beans.factory.DependencyDescriptor;
import com.chy.summer.framework.beans.support.RootBeanDefinition;
import com.chy.summer.framework.beans.support.annotation.InjectionMetadata;
import com.chy.summer.framework.core.BridgeMethodResolver;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.exception.BeanCreationException;
import com.chy.summer.framework.exception.BeansException;
import com.chy.summer.framework.util.*;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AutowiredAnnotationBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware {

    /**
     * InjectionMetadata 的缓存
     */
    private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);


    private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<>(4);

    private final Map<Class<?>, Constructor<?>[]> candidateConstructorsCache = new ConcurrentHashMap<>(256);

    private String requiredParameterName = "required";

    private boolean requiredParameterValue = true;

    private ConfigurableListableBeanFactory beanFactory;

    public AutowiredAnnotationBeanPostProcessor() {
        this.autowiredAnnotationTypes.add(Autowired.class);
        this.autowiredAnnotationTypes.add(Value.class);
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        //获取要注入的元数据对象,这里面保存了 那些已经被打了 @Autowired 注解的 元素和方法
        InjectionMetadata metadata = findAutowiringMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs);
        } catch (BeanCreationException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, " 注入属性异常 ", ex);
        }
        return pvs;
    }


    private InjectionMetadata findAutowiringMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        //如果缓存是 Null，或者 metadata 里面的class和我传入的class不同，就需要刷新缓存
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    metadata = buildAutowiringMetadata(clazz);
                    this.injectionMetadataCache.put(cacheKey, metadata);
                }
            }
        }
        return metadata;
    }

    /**
     * 用class来构造 InjectionMetadata 对象
     *
     * @param clazz
     * @return
     */
    private InjectionMetadata buildAutowiringMetadata(Class<?> clazz) {
        List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
        Class<?> targetClass = clazz;

        do {
            final List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();

            //反射获取 目标class 里的所有属性,然后走这个匿名内部类的自定义方法
            ReflectionUtils.doWithLocalFields(targetClass, field -> {
                AnnotationAttributes ann = findAutowiredAnnotation(field);
                if (ann == null) {
                    return;
                }
                if (Modifier.isStatic(field.getModifiers())) {
                    log.warn("自动注入不支持 静态变量 : {}", field);
                    return;
                }
                boolean required = determineRequiredStatus(ann);
                currElements.add(new AutowiredFieldElement(field, required));
            });

            //反射获取 目标class 里的所有方法,然后走这个匿名内部类的自定义方法
            ReflectionUtils.doWithLocalMethods(targetClass, method -> {

                Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
                //这里绕过了所有的桥接方法
                if (!BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                    return;
                }
                AnnotationAttributes ann = findAutowiredAnnotation(bridgedMethod);
                //getMostSpecificMethod 可以从代理对象的方法上面，找到真正的方法，这里主要就是判断这不是一个代理过得方法
                if (ann != null && method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        log.warn("Autowired 注解不支持 静态方法 ： {}", method);
                        return;
                    }
                    if (method.getParameterCount() == 0) {
                        log.warn("Autowired 注解作用的方法最少要有一个参数 ： {}", method);
                    }
                    boolean required = determineRequiredStatus(ann);
                    //获取对应 getter setter 方法的 PropertyDescriptor
                    PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
                    currElements.add(new AutowiredMethodElement(method, required, pd));
                }
            });

            elements.addAll(0, currElements);
            //获取他的父类 再处理一遍,直到没有父类,或者父类是 Obejct
            targetClass = targetClass.getSuperclass();
        }
        while (targetClass != null && targetClass != Object.class);

        return new InjectionMetadata(clazz, elements);
    }

    /**
     * 从属性上面去 检查有没有 @Autowired 或者 @Value 注解
     * 如果有的话，把注解里面的属性值放在 AnnotationAttributes 对象里返回
     */
    private AnnotationAttributes findAutowiredAnnotation(AccessibleObject ao) {
        if (ao.getAnnotations().length > 0) {
            for (Class<? extends Annotation> type : this.autowiredAnnotationTypes) {
                AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(ao, type);
                if (attributes != null) {
                    return attributes;
                }
            }
        }
        return null;
    }


    private boolean determineRequiredStatus(AnnotationAttributes ann) {
        //如果不是  @Autowired 注解 就直接 返回了
        if (!ann.containsKey(this.requiredParameterName)) {
            return true;
        }
        //如果是 @Autowired 注解 就判断一下 required 这个属性的值
        Boolean required = ann.getRequiredAttribute(this.requiredParameterName, Boolean.class);
        return required == this.requiredParameterValue;
    }


    private class AutowiredFieldElement extends InjectionMetadata.InjectedElement {

        private final boolean required;

        private volatile boolean cached = false;

        private volatile Object cachedFieldValue;

        public AutowiredFieldElement(Field field, boolean required) {
            super(field, null);
            this.required = required;
        }

        @Override
        public void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
            Field field = (Field) this.member;
            Object value;
            DependencyDescriptor desc = new DependencyDescriptor(field, this.required);
            desc.setContainingClass(bean.getClass());
            Set<String> autowiredBeanNames = new LinkedHashSet<>(1);
            Assert.state(beanFactory != null, "beanFactory 不能是Null");

            //获取属性上要注入对象的 实例
            value = beanFactory.resolveDependency(desc, beanName, autowiredBeanNames);

            //如果获取到了,就用反射把他设置进去
            if (value != null) {
                ReflectionUtils.makeAccessible(field);
                field.set(bean, value);
            }
        }


    }

    private class AutowiredMethodElement extends InjectionMetadata.InjectedElement {

        private final boolean required;

        private volatile boolean cached = false;

        private volatile Object[] cachedMethodArguments;

        public AutowiredMethodElement(Method method, boolean required, PropertyDescriptor pd) {
            super(method, pd);
            this.required = required;
        }

    }


    /**
     * 在生成bean实例的时候,用来选择构造器的
     *
     * @param beanClass
     * @param beanName
     * @return
     * @throws BeansException
     * @see com.chy.summer.framework.beans.support.AbstractAutowireCapableBeanFactory#createBeanInstance(String, RootBeanDefinition, Object[])
     */
    @Override
    public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeansException {

        //构造器的缓存
        Constructor<?>[] candidateConstructors = this.candidateConstructorsCache.get(beanClass);
        if (candidateConstructors != null) {
            return constructorResultHandle(candidateConstructors);
        }

        //上锁去class上获取构造器,这里常规双检查
        synchronized (this.candidateConstructorsCache) {
            candidateConstructors = this.candidateConstructorsCache.get(beanClass);
            if (candidateConstructors != null) {
                return constructorResultHandle(candidateConstructors);
            }

            Constructor<?>[] rawCandidates;
            try {
                //反射获取了 所有的构造器
                rawCandidates = beanClass.getDeclaredConstructors();
            } catch (Throwable ex) {
                throw new BeanCreationException(beanName, "获取构造器失败", ex);
            }

            //如果只有一个构造器那么情况就简单很多了
            if (rawCandidates.length == 1) {
                //如果这个唯一的构造器里带有参数,那么就把这个构造器给保存下来
                if (rawCandidates[0].getParameterCount() > 0) {
                    candidateConstructors = new Constructor<?>[]{rawCandidates[0]};
                } else {
                    //如果唯一的构造器是一个无参构造,那么其实这里 可以直接返回Null,不过这里创建一个空数据是为了能够缓存
                    //最后经过 constructorResultHandle 处理这个 空数据会变成Null返回
                    candidateConstructors = new Constructor<?>[0];
                }
                this.candidateConstructorsCache.put(beanClass, candidateConstructors);
                return constructorResultHandle(candidateConstructors);
            }

            //下面就是多构造器的情况了
            List<Constructor<?>> candidates = new ArrayList<>(rawCandidates.length);
            Constructor<?> requiredConstructor = null;
            Constructor<?> defaultConstructor = null;
            //如果一个class 里有多个构造器的话,需要用 @Autowired 来标注一下,到底是选择哪一个构造器
            for (Constructor<?> candidate : rawCandidates) {
                //去构造器上找有没打了 @Autowired 注解
                AnnotationAttributes annotationAttributes = findAutowiredAnnotationByConstructor(candidate, beanClass);
                if (annotationAttributes != null) {
                    if (requiredConstructor != null) {
                        throw new BeanCreationException(beanName,
                                "在一个class里不能有多个构造器同时标注 @Autowired");
                    }
                    requiredConstructor = candidate;
                    candidates.add(candidate);
                }
            }

            if (!candidates.isEmpty()) {
                candidateConstructors = candidates.toArray(new Constructor<?>[0]);
            }else{
                candidateConstructors = new Constructor<?>[0];
            }
            this.candidateConstructorsCache.put(beanClass, candidateConstructors);
        }
        return constructorResultHandle(candidateConstructors);
    }


    /**
     * 去构造器上检查有没有 @Autowired 注解有的话把他的属性给拉下来
     * 这里 对 findAutowiredAnnotation 方法的一个包装
     * 是因为 可能传入的 constructor 是一个代理对象的方法,如果代理对象上没不到对应的注解,会去拿真实为代理的方法
     * @param constructor
     * @return
     */
    private AnnotationAttributes findAutowiredAnnotationByConstructor(Constructor<?> constructor , Class<?> beanClass){
        AnnotationAttributes autowiredAnnotation = findAutowiredAnnotation(constructor);
        //如果没拿到对应的注解,那么就拨开代理对象这层皮
        if (autowiredAnnotation == null) {
            Class<?> userClass = ClassUtils.getUserClass(beanClass);
            if (userClass != beanClass) {
                try {
                    Constructor<?> superCtor =
                            userClass.getDeclaredConstructor(constructor.getParameterTypes());
                    autowiredAnnotation = findAutowiredAnnotation(superCtor);
                }
                catch (NoSuchMethodException ex) {
                }
            }
        }

        return autowiredAnnotation;
    }


    /**
     * 用来处理 this#determineCandidateConstructors 返回结果的
     * 如果 数组里面没有元素会直接返回null;
     *
     * @return
     */
    private Constructor<?>[] constructorResultHandle(Constructor<?>[] constructors) {
        if (constructors == null || constructors.length == 0) {
            return null;
        }
        return constructors;
    }


}
