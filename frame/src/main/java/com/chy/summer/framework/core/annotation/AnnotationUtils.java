package com.chy.summer.framework.core.annotation;


import com.chy.summer.framework.annotation.constant.AnnotationConstant;
import com.chy.summer.framework.annotation.core.AliasFor;
import com.chy.summer.framework.beans.config.BeanDefinitionHolder;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.factory.ContextAnnotationAutowireCandidateResolver;
import com.chy.summer.framework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import com.chy.summer.framework.beans.support.DefaultListableBeanFactory;
import com.chy.summer.framework.beans.support.RootBeanDefinition;
import com.chy.summer.framework.context.annotation.ConfigurationClassPostProcessor;
import com.chy.summer.framework.core.BridgeMethodResolver;
import com.chy.summer.framework.core.ordered.AnnotationAwareOrderComparator;
import com.chy.summer.framework.util.*;
import com.sun.istack.internal.Nullable;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public  abstract class AnnotationUtils {

    private static Map<String,List<Method>>  annotationMethodCache = new ConcurrentHashMap<>();

    private static final Map<AnnotationCacheKey, Annotation> findAnnotationCache =
            new ConcurrentHashMap<>(256);

    private static final Map<Class<? extends Annotation>, Boolean> synthesizableCache =
            new ConcurrentReferenceHashMap<>(256);

    private static final Map<Method, AliasDescriptor> aliasDescriptorCache =
            new ConcurrentReferenceHashMap<>(256);

    private static final Map<AnnotationCacheKey, Boolean> metaPresentCache =
            new ConcurrentReferenceHashMap<>(256);

    private static final Map<Class<? extends Annotation>, Map<String, List<String>>> attributeAliasesCache =
            new ConcurrentReferenceHashMap<>(256);


    /**
     * 注解与注解类的参数的映射关系
     */
    private static final Map<Class<? extends Annotation>, List<Method>> attributeMethodsCache =
            new ConcurrentReferenceHashMap<>(256);

    public static final String CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME =
            "chy.summer.internalConfigurationAnnotationProcessor";

    public static final String AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME =
            "chy.summer.internalAutowiredAnnotationProcessor";



    /**
     * 把注解里的属性给抽出来放入 AnnotationAttributes
     * 如果这里有属性需要继承下去,就把相关任务放入 LinkList 中,这里是有顺序要求的
     * @annotationValue 这里根据不同的类型执行不同的策略,是注解类型就反射拿,是map类型就put拿
     * @annotationClass 要解析的注解的类型
     * @aliasForTaskList 如果属性上有打了注解 @AliasFor 则生成任务放入队列里,如果为null则不会解析 @AliasFor
     */
    public static AnnotationAttributes pareAnnotationToAttributes(Object value,
                                                                   Class<? extends Annotation> annotationClass,
                                                                   List<AliasForTask> aliasForTaskList) {

        AnnotationAttributes attributes = new AnnotationAttributes(annotationClass.getName());
        List<Method> methodsByCache = getMethodsByCache(annotationClass);
        methodsByCache.stream().forEach(method -> {

            //通过value的类型拿到真正的 annotationValue
            Object annotationValue = getAnnotationValue(value,method);

            Object defaultValue = method.getDefaultValue();
            attributes.put(method.getName(),annotationValue,defaultValue);
            if(aliasForTaskList == null){return;}
            //判断上这个属性上是否有 @AliasFor 注解,有的话这个方法上的值将会传递给对应的注解上
            AliasFor aliasFor = method.getAnnotation(AliasFor.class);
            if(aliasFor == null){return;}
            aliasForTaskList.add(new AliasForTask(aliasFor,method.getName(),annotationClass));
        });
        return attributes;
    }

    /**
     * 从类上获取注解的所有信息,包括继承关系,属性等
     * @clazz 要获取的类
     * @attributesMap 返回参数, 处理的结果会放入这个字典中,请传入一个 空的字典对象进去 key:注解名称,value:对应注解的属性值
     * @shouldSuper 是否连这个类的父类和接口上的注解一起扫描
     */
    public static Map<String, Set<String>> getAnnotationInfoByClass(Class clazz,
                                                                    Map<String,AnnotationAttributes> attributesMap,
                                                                    boolean shouldSuper){

        List<AliasForTask> aliasForTaskList = new LinkedList<>();
        Map<String, Set<String>> result = null;
        if(shouldSuper){
            result = doGetAnnotationInfoByClassAll(clazz,aliasForTaskList,attributesMap);
        }else {
            result = doGetAnnotationInfoByClass(clazz,aliasForTaskList,attributesMap);
        }
        doAliasForTask(aliasForTaskList,attributesMap);
        return result;
    }



    private static Map<String, Set<String>> doGetAnnotationInfoByClassAll(Class clazz,
                                                                          List<AliasForTask> aliasForTaskList,
                                                                          Map<String, AnnotationAttributes> attributesMap){

        //开始解析,拿到这个类的注解 树状图
        Map<String, Set<String>> classAnnotationTree =
                doGetAnnotationInfoByClass(clazz, aliasForTaskList, attributesMap);


        Class superclass = clazz.getSuperclass();
        //排除掉 Object 这些公用的父类对象后递归处理父类对象
        if(superclass !=null && superclass.getName().startsWith("java.")){
            Map<String, Set<String>> superTree =
                    doGetAnnotationInfoByClassAll(superclass, aliasForTaskList, attributesMap);
            //把父类上的注解给综合一下
            classAnnotationTree.putAll(superTree);
        }
        //然后处理实现的接口
        for (Class anInterface : clazz.getInterfaces()) {
            Map<String, Set<String>> stringSetMap =
                    doGetAnnotationInfoByClassAll(anInterface, aliasForTaskList, attributesMap);
            //把接口上的也综合一下
            classAnnotationTree.putAll(stringSetMap);
        }

        return classAnnotationTree;
    }



    /**
     * 解析 class 获取这个类上所有注解的关系,以及属性,这个方法只会扫描当前类上面的,父类和接口上面的注解不会处理
     * @return  key:这个类上的注解的名称 value:这个注解上面继承的所有注解的名称
     */
    public static Map<String,Set<String>> doGetAnnotationInfoByClass(Class clazz,
                                                  List<AliasForTask> aliasForTaskList,
                                                  Map<String, AnnotationAttributes> attributesMap){
        Map<String,Set<String>> result = new HashMap();
        for (Annotation annotation : clazz.getAnnotations()) {
            //解析注解,返回值是 这个注解后面还继承的所有注解的名字
            Set<String> annotationTree = metaAnnotationMapHandle(annotation.annotationType(),
                    annotation, aliasForTaskList, attributesMap, null);
            result.put(annotation.annotationType().getName(),annotationTree);
        }
        return result;
    }


    /**
     * 执行注解属性继承关系的任务
     * @aliasForTaskList 要执行的任务的列表
     * @attributesMap key:注解名字 value:这个注解里的所有属性 任务里涉及到的所有注解这个字典里都必须要有
     */
    public static void doAliasForTask(List<AliasForTask> aliasForTaskList,
                                       Map<String, AnnotationAttributes> attributesMap){
        for (AliasForTask aliasForTask : aliasForTaskList) {
            AnnotationAttributes targetAttributes = attributesMap.get(aliasForTask.getTargerClass().getName());
            AnnotationAttributes formAttributes = attributesMap.get(aliasForTask.getFormClass().getName());

            targetAttributes.update(aliasForTask.getTargerName(),
                    formAttributes.getAttributeValue(aliasForTask.getFormName()));
        }

    }



    /**
     * 解析注解的继承关系，把他的继承关系存入 metaAnnotationMap
     * @annotationType 要解析的注解的类型
     * @annotationAttributes 这个注解里面填入的属性值 可以手动提取以map 的形式存在,也可以直接传入 一个 annotation对象,用反射获取
     * @aliasForTaskList 返回参数 因为注解会存在属性继承覆盖的关系,这边会把所有的继承任务放入这个属性
     * @attributesMap  返回参数 key:注解名字 value:这个注解里的所有属性(注解会有继承)
     * @result 返回参数 会把这个注解下面所有继承的注解的名字放进去
     *
     */
    public static Set<String> metaAnnotationMapHandle(Class<? extends Annotation> annotationType,
                                                Object annotationAttributes,
                                                List<AliasForTask> aliasForTaskList,
                                                Map<String, AnnotationAttributes> attributesMap,
                                                Set<String> result){
        if(result == null){
            //这是根目录进来
            result = new LinkedHashSet<>();
            //解析root注解上面的属性
            AnnotationAttributes attributes = pareAnnotationToAttributes(annotationAttributes,annotationType,aliasForTaskList);
            attributesMap.put(annotationType.getName(),attributes);
        }else{
            result.add(annotationType.getName());
        }

        //获取了这个注解上面的父注解
        Annotation[] annotations = annotationType.getAnnotations();
        Set<String> finalResult = result;
        //递归扫描注解上面的注解,同时过滤掉一些不需要解析的注解
        Arrays.stream(annotations).filter(annotation -> {
            return !AnnotationConstant.ignoreAnnotation.contains(annotation.annotationType());
        }).forEach(annotation ->{
            Class<? extends Annotation> type = annotation.annotationType();
            //解析注解上面的属性
            AnnotationAttributes attributes = AnnotationUtils.pareAnnotationToAttributes(annotation,type,aliasForTaskList);
            metaAnnotationMapHandle(type,annotationAttributes,aliasForTaskList,attributesMap, finalResult);
            attributesMap.put(type.getName(),attributes);
        });

        return result;
    }



    private static Object getAnnotationValue(Object value,Method method){
        if(value instanceof Annotation){
          return  getAnnotationValueByAnnotation((Annotation) value,method);
        }

        if(value instanceof Map){
           return getAnnotationValueByMap((Map<String, Object>) value,method);
        }



        return null;
    }


    private static Object getAnnotationValueByMap(Map<String,Object> map,Method method){
        String name = method.getName();
        return map.get(name);
    }

    private static Object getAnnotationValueByAnnotation(Annotation annotation,Method method){
        Object annotationValue = null;
        try {
            //获取注解里的属性值
            annotationValue = method.invoke(annotation);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return annotationValue;
    }



    /**
     * 获取注解里所有方法,这里会先过滤,然后有缓存
     * @return
     */
    static List<Method> getMethodsByCache(Class<? extends Annotation> annotationClass){
        List<Method> result = annotationMethodCache.get(annotationClass.getName());
        if(result == null){
            Method[] methods = annotationClass.getDeclaredMethods();
            result = new ArrayList<>(methods.length);
            final List fresult = result;
            Arrays.stream(methods).filter(AnnotationUtils::isAttributeMethod)
                    .forEach(method -> {
                        method.setAccessible(true);
                        fresult.add(method);
                    });
            annotationMethodCache.put(annotationClass.getName(),result);
        }

        return result;

    }


    /**
     * 判断 注解里的某个方法是不是属性
     * @param method
     * @return
     */
    static boolean isAttributeMethod( Method method) {
        return (method != null && method.getParameterCount() == 0 && method.getReturnType() != void.class);
    }

    /**
     * 在给定的类上找到相应的注解，遍历其接口、注解，如果注解没有直接出现在给定类本身上将遍历它的父类。
     */
    @Nullable
    public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
        Assert.notNull(clazz, "Class must not be null");
        if (annotationType == null) {
            return null;
        }

        /**
         * 缓存的key 类+注解 确定一个key
         */
        AnnotationCacheKey cacheKey = new AnnotationCacheKey(clazz, annotationType);
        A result = (A) findAnnotationCache.get(cacheKey);
        if (result == null) {
            result = findAnnotation(clazz, annotationType, new HashSet<>());
            if (result != null) {
                findAnnotationCache.put(cacheKey, result);
            }
        }
        return result;
    }

    private static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType, Set<Annotation> visited) {
        try {
            //直接去拿类上的注解,拿到了就直接返回
            A annotation = clazz.getDeclaredAnnotation(annotationType);
            if (annotation != null) {
                return annotation;
            }
            //这里没直接拿到,递归判断,注解上的注解.
            for (Annotation declaredAnn : clazz.getDeclaredAnnotations()) {
                Class<? extends Annotation> declaredType = declaredAnn.annotationType();
                if (!isInJavaLangAnnotationPackage(declaredType) && visited.add(declaredAnn)) {
                    annotation = findAnnotation(declaredType, annotationType, visited);
                    if (annotation != null) {
                        return annotation;
                    }
                }
            }
        }
        catch (Throwable ex) {
            log.warn("获取类 [%s] 上的注解 [%s] 失败",clazz,annotationType);
            return null;
        }

        for (Class<?> ifc : clazz.getInterfaces()) {
            A annotation = findAnnotation(ifc, annotationType, visited);
            if (annotation != null) {
                return annotation;
            }
        }

        Class<?> superclass = clazz.getSuperclass();
        if (superclass == null || superclass == Object.class) {
            return null;
        }
        return findAnnotation(superclass, annotationType, visited);
    }


    /**
     * 判断这个注解是不是 jdk 里面定义注解的那几个注解,比如 @Documented @Target
     * @param annotationType
     * @return
     */
    static boolean isInJavaLangAnnotationPackage(@Nullable Class<? extends Annotation> annotationType) {
        return (annotationType != null && annotationType.getName().startsWith("java.lang.annotation"));
    }


    /**
     * 在给定的方法上找到相应的注解，如果注解没有直接出现在给定方法上将遍历它的父方法或者父接口。
     */
    @Nullable
    public static <A extends Annotation> A findAnnotation(Method method, @Nullable Class<A> annotationType) {
        //TODO GYX 尚未实现
        return null;
    }


    /**
     * 注册一些 通用的 beanFactroyPostProcessor 前置处理器 进入ioc容器
     * @param registry
     * @param source
     */
    public static void registerAnnotationConfigProcessors(BeanDefinitionRegistry registry, Object source) {
        //把BeanDefinitionRegistry 根据类型 强转到DefaultListableBeanFactory
        DefaultListableBeanFactory beanFactory = unwrapDefaultListableBeanFactory(registry);
        //先给 DefaultListableBeanFactory 设置一些 属性
        if (beanFactory != null) {
            if (!(beanFactory.getDependencyComparator() instanceof AnnotationAwareOrderComparator)) {
                beanFactory.setDependencyComparator(AnnotationAwareOrderComparator.INSTANCE);
            }
            if (!(beanFactory.getAutowireCandidateResolver() instanceof ContextAnnotationAutowireCandidateResolver)) {
                beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
            }
        }

        Set<BeanDefinitionHolder> beanDefs = new LinkedHashSet<>(8);

        //把 ConfigurationClassPostProcessor 给注册进入 ioc 容器
        //这个类是一个 BeanDefinitionRegistryPostProcessor , 他会去扫描对应的 路径下的所有类生成 BeanDefinition
        if (!registry.containsBeanDefinition(CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)) {
            RootBeanDefinition def = new RootBeanDefinition(ConfigurationClassPostProcessor.class);
            beanDefs.add(registerPostProcessor(registry, def, CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME));
        }

        //把 AutowiredAnnotationBeanPostProcessor 给注册进入 IOC 容器

        if (!registry.containsBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
            RootBeanDefinition def = new RootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class);
            beanDefs.add(registerPostProcessor(registry, def, AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
        }




    }

    /**
     *  把对应的 BeanDefinition 给注册进入 IOC 容器
     * @param registry
     * @param definition
     * @param beanName
     * @return
     */
    private static BeanDefinitionHolder registerPostProcessor(
            BeanDefinitionRegistry registry, RootBeanDefinition definition, String beanName) {
        //注册
        registry.registerBeanDefinition(beanName, definition);
        return new BeanDefinitionHolder(definition, beanName);
    }

    @Nullable
    public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationType) {
        Method resolvedMethod = BridgeMethodResolver.findBridgedMethod(method);
        return getAnnotation((AnnotatedElement) resolvedMethod, annotationType);
    }

    /**
     * 在注解上查找指定类型的注解
     */
    @Nullable
    public static <A extends Annotation> A getAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
        try {
            //获取注解类上的所有注解
            A annotation = annotatedElement.getAnnotation(annotationType);
            if (annotation == null) {
                for (Annotation metaAnn : annotatedElement.getAnnotations()) {
                    //找到需要的注解
                    annotation = metaAnn.annotationType().getAnnotation(annotationType);
                    if (annotation != null) {
                        break;
                    }
                }
            }
            return (annotation != null ? synthesizeAnnotation(annotation, annotatedElement) : null);
        }
        catch (Throwable ex) {
            //额外的异常抛出
            //handleIntrospectionFailure(annotatedElement, ex);
            return null;
        }
    }

    /**
     * 合成注解
     */
    public static <A extends Annotation> A synthesizeAnnotation(
            A annotation, @Nullable AnnotatedElement annotatedElement) {

        return synthesizeAnnotation(annotation, (Object) annotatedElement);
    }

    /**
     * 合成注解
     */
    static <A extends Annotation> A synthesizeAnnotation(A annotation, @Nullable Object annotatedElement) {
        Assert.notNull(annotation, "Annotation不可为空");
        if (annotation instanceof SynthesizedAnnotation) {
            return annotation;
        }

        Class<? extends Annotation> annotationType = annotation.annotationType();
        if (!isSynthesizable(annotationType)) {
            return annotation;
        }
        //创建注释属性提取器
        DefaultAnnotationAttributeExtractor attributeExtractor =
                new DefaultAnnotationAttributeExtractor(annotation, annotatedElement);
        InvocationHandler handler = new SynthesizedAnnotationInvocationHandler(attributeExtractor);

        //创建合成注解
        Class<?>[] exposedInterfaces = new Class<?>[] {annotationType, SynthesizedAnnotation.class};
        return (A) Proxy.newProxyInstance(annotation.getClass().getClassLoader(), exposedInterfaces, handler);
    }

    /**
     * 将注解数组合并
     */
    static Annotation[] synthesizeAnnotationArray(
            Annotation[] annotations, @Nullable Object annotatedElement) {

        Annotation[] synthesized = (Annotation[]) Array.newInstance(
                annotations.getClass().getComponentType(), annotations.length);
        for (int i = 0; i < annotations.length; i++) {
            synthesized[i] = synthesizeAnnotation(annotations[i], annotatedElement);
        }
        return synthesized;
    }

    /**
     * 判断提供的方法是否为“ annotationType”方法
     */
    static boolean isAnnotationTypeMethod(@Nullable Method method) {
        return (method != null && method.getName().equals("annotationType") && method.getParameterCount() == 0);
    }

    /**
     * 判断提供的注释类型的注释是否可合成，即是否需要包装在提供功能高于标准JDK注释的动态代理中
     * @param annotationType 需要判断的注解类型
     * @return
     */
    private static boolean isSynthesizable(Class<? extends Annotation> annotationType) {
        //尝试从缓存中获取结果
        Boolean synthesizable = synthesizableCache.get(annotationType);
        if (synthesizable != null) {
            return synthesizable;
        }

        synthesizable = Boolean.FALSE;
        //找到注解参数
        for (Method attribute : getAttributeMethods(annotationType)) {
            if (!getAttributeAliasNames(attribute).isEmpty()) {
                synthesizable = Boolean.TRUE;
                break;
            }
            Class<?> returnType = attribute.getReturnType();
            if (Annotation[].class.isAssignableFrom(returnType)) {
                Class<? extends Annotation> nestedAnnotationType =
                        (Class<? extends Annotation>) returnType.getComponentType();
                if (isSynthesizable(nestedAnnotationType)) {
                    synthesizable = Boolean.TRUE;
                    break;
                }
            }
            else if (Annotation.class.isAssignableFrom(returnType)) {
                Class<? extends Annotation> nestedAnnotationType = (Class<? extends Annotation>) returnType;
                if (isSynthesizable(nestedAnnotationType)) {
                    synthesizable = Boolean.TRUE;
                    break;
                }
            }
        }

        synthesizableCache.put(annotationType, synthesizable);
        return synthesizable;
    }

    /**
     * 获取注解的属性别名
     * @param annotationType
     * @return
     */
    static Map<String, List<String>> getAttributeAliasMap(@Nullable Class<? extends Annotation> annotationType) {
        if (annotationType == null) {
            return Collections.emptyMap();
        }

        Map<String, List<String>> map = attributeAliasesCache.get(annotationType);
        if (map != null) {
            return map;
        }

        map = new LinkedHashMap<>();
        for (Method attribute : getAttributeMethods(annotationType)) {
            List<String> aliasNames = getAttributeAliasNames(attribute);
            if (!aliasNames.isEmpty()) {
                map.put(attribute.getName(), aliasNames);
            }
        }

        attributeAliasesCache.put(annotationType, map);
        return map;
    }

    /**
     * 给定类注释类型，检索指定属性的默认值
     * @param annotationType 注释类型
     * @param attributeName 属性名
     * @return
     */
    @Nullable
    public static Object getDefaultValue(
            @Nullable Class<? extends Annotation> annotationType, @Nullable String attributeName) {

        if (annotationType == null || !StringUtils.hasText(attributeName)) {
            return null;
        }
        try {
            return annotationType.getDeclaredMethod(attributeName).getDefaultValue();
        }
        catch (Throwable ex) {
            //抛出额外的错误
//            handleIntrospectionFailure(annotationType, ex);
            return null;
        }
    }

    /**
     * 获取在提供的annotationType中声明方法（注解的参数）
     * @param annotationType
     * @return
     */
    static List<Method> getAttributeMethods(Class<? extends Annotation> annotationType) {
        //尝试从缓存中获取
        List<Method> methods = attributeMethodsCache.get(annotationType);
        if (methods != null) {
            return methods;
        }

        methods = new ArrayList<>();
        for (Method method : annotationType.getDeclaredMethods()) {
            //逐一判断方法是否符合注解要求（没有参数，没有返回值）
            if (isAttributeMethod(method)) {
                ReflectionUtils.makeAccessible(method);
                methods.add(method);
            }
        }

        attributeMethodsCache.put(annotationType, methods);
        return methods;
    }

    /**
     * 获取通过@AliasFor为所提供的注释属性 配置的别名属性
     */
    static List<String> getAttributeAliasNames(Method attribute) {
        Assert.notNull(attribute, "attribute不可为空");
        AliasDescriptor descriptor = AliasDescriptor.from(attribute);
        return (descriptor != null ? descriptor.getAttributeAliasNames() : Collections.<String> emptyList());
    }

    private static DefaultListableBeanFactory unwrapDefaultListableBeanFactory(BeanDefinitionRegistry registry) {
        if (registry instanceof DefaultListableBeanFactory) {
            return (DefaultListableBeanFactory) registry;
        }
        else {
            return null;
        }
    }

    /**
     * 判断metaAnnotationType注释元是否存在
     * @param annotationType
     * @param metaAnnotationType
     * @return
     */
    public static boolean isAnnotationMetaPresent(Class<? extends Annotation> annotationType,
                                                  @Nullable Class<? extends Annotation> metaAnnotationType) {
        Assert.notNull(annotationType, "AnnotationType不可为空");
        if (metaAnnotationType == null) {
            return false;
        }
        //创建一个缓存对象用于查询
        AnnotationCacheKey cacheKey = new AnnotationCacheKey(annotationType, metaAnnotationType);
        //尝试从缓存获取
        Boolean metaPresent = metaPresentCache.get(cacheKey);
        if (metaPresent != null) {
            return metaPresent;
        }
        metaPresent = Boolean.FALSE;
        if (findAnnotation(annotationType, metaAnnotationType, false) != null) {
            metaPresent = Boolean.TRUE;
        }
        metaPresentCache.put(cacheKey, metaPresent);
        return metaPresent;
    }

    /**
     * 在给定的类上找到相应的注解
     */
    private static <A extends Annotation> A findAnnotation(
            Class<?> clazz, @Nullable Class<A> annotationType, boolean synthesize) {

        Assert.notNull(clazz, "Class不可为空");
        if (annotationType == null) {
            return null;
        }

        AnnotationCacheKey cacheKey = new AnnotationCacheKey(clazz, annotationType);
        A result = (A) findAnnotationCache.get(cacheKey);
        if (result == null) {
            result = findAnnotation(clazz, annotationType, new HashSet<>());
            if (result != null && synthesize) {
                //合成注解
                result = synthesizeAnnotation(result, clazz);
                findAnnotationCache.put(cacheKey, result);
            }
        }
        return result;
    }

    private static class AliasDescriptor {

        private final Method sourceAttribute;

        private final Class<? extends Annotation> sourceAnnotationType;

        private final String sourceAttributeName;

        private final Method aliasedAttribute;

        private final Class<? extends Annotation> aliasedAnnotationType;

        private final String aliasedAttributeName;

        private final boolean isAliasPair;

        /**
         * 在提供的注释属性上从@AliasFor的声明中创建一个AliasDescriptor，并验证@AliasFor的配置
         * @param attribute @AliasFor注释的注释属性
         */
        @Nullable
        public static AliasDescriptor from(Method attribute) {
            //尝试从缓存中获取AliasDescriptor
            AliasDescriptor descriptor = aliasDescriptorCache.get(attribute);
            if (descriptor != null) {
                return descriptor;
            }
            //验证AliasFor注解
            AliasFor aliasFor = attribute.getAnnotation(AliasFor.class);
            if (aliasFor == null) {
                return null;
            }
            //创建一个新AliasDescriptor
            descriptor = new AliasDescriptor(attribute, aliasFor);
            descriptor.validate();
            aliasDescriptorCache.put(attribute, descriptor);
            return descriptor;
        }

        /**
         * 创建一个新AliasDescriptor
         *
         * @param sourceAttribute
         * @param aliasFor
         */
        private AliasDescriptor(Method sourceAttribute, AliasFor aliasFor) {
            Class<?> declaringClass = sourceAttribute.getDeclaringClass();
            Assert.isTrue(declaringClass.isAnnotation(), "源属性必须来自注释");
            //源属性的持有
            this.sourceAttribute = sourceAttribute;
            //源属性的类型
            this.sourceAnnotationType = (Class<? extends Annotation>) declaringClass;
            //源属性的名称
            this.sourceAttributeName = sourceAttribute.getName();
            //设置合并注解的类型
            this.aliasedAnnotationType = (Annotation.class == aliasFor.annotation() ?
                    this.sourceAnnotationType : aliasFor.annotation());
            //设置合并注解的名称
            this.aliasedAttributeName = getAliasedAttributeName(aliasFor, sourceAttribute);
            if (this.aliasedAnnotationType == this.sourceAnnotationType &&
                    this.aliasedAttributeName.equals(this.sourceAttributeName)) {
                String msg = String.format("@AliasFor声明注释[%s]中的属性'%s'指向自身。指定'annotation'指向元注释上的同名属性。",
                        sourceAttribute.getName(), declaringClass.getName());
                throw new AnnotationConfigurationException(msg);
            }
            try {
                //设置别名属性
                this.aliasedAttribute = this.aliasedAnnotationType.getDeclaredMethod(this.aliasedAttributeName);
            }
            catch (NoSuchMethodException ex) {
                String msg = String.format(
                        "注释[%s]中的属性'%s'声明为@AliasFor，因为注释[%s]中不存在属性'%s'",
                        this.sourceAttributeName, this.sourceAnnotationType.getName(), this.aliasedAttributeName,
                        this.aliasedAnnotationType.getName());
                throw new AnnotationConfigurationException(msg, ex);
            }

            this.isAliasPair = (this.sourceAnnotationType == this.aliasedAnnotationType);
        }

        private void validate() {
            // Target annotation is not meta-present?
            if (!this.isAliasPair && !isAnnotationMetaPresent(this.sourceAnnotationType, this.aliasedAnnotationType)) {
                String msg = String.format("@AliasFor declaration on attribute '%s' in annotation [%s] declares " +
                                "an alias for attribute '%s' in meta-annotation [%s] which is not meta-present.",
                        this.sourceAttributeName, this.sourceAnnotationType.getName(), this.aliasedAttributeName,
                        this.aliasedAnnotationType.getName());
                throw new AnnotationConfigurationException(msg);
            }

            if (this.isAliasPair) {
                AliasFor mirrorAliasFor = this.aliasedAttribute.getAnnotation(AliasFor.class);
                if (mirrorAliasFor == null) {
                    String msg = String.format("Attribute '%s' in annotation [%s] must be declared as an @AliasFor [%s].",
                            this.aliasedAttributeName, this.sourceAnnotationType.getName(), this.sourceAttributeName);
                    throw new AnnotationConfigurationException(msg);
                }

                String mirrorAliasedAttributeName = getAliasedAttributeName(mirrorAliasFor, this.aliasedAttribute);
                if (!this.sourceAttributeName.equals(mirrorAliasedAttributeName)) {
                    String msg = String.format("Attribute '%s' in annotation [%s] must be declared as an @AliasFor [%s], not [%s].",
                            this.aliasedAttributeName, this.sourceAnnotationType.getName(), this.sourceAttributeName,
                            mirrorAliasedAttributeName);
                    throw new AnnotationConfigurationException(msg);
                }
            }

            Class<?> returnType = this.sourceAttribute.getReturnType();
            Class<?> aliasedReturnType = this.aliasedAttribute.getReturnType();
            if (returnType != aliasedReturnType &&
                    (!aliasedReturnType.isArray() || returnType != aliasedReturnType.getComponentType())) {
                String msg = String.format("Misconfigured aliases: attribute '%s' in annotation [%s] " +
                                "and attribute '%s' in annotation [%s] must declare the same return type.",
                        this.sourceAttributeName, this.sourceAnnotationType.getName(), this.aliasedAttributeName,
                        this.aliasedAnnotationType.getName());
                throw new AnnotationConfigurationException(msg);
            }

            if (this.isAliasPair) {
                validateDefaultValueConfiguration(this.aliasedAttribute);
            }
        }

        private void validateDefaultValueConfiguration(Method aliasedAttribute) {
            Assert.notNull(aliasedAttribute, "aliasedAttribute must not be null");
            Object defaultValue = this.sourceAttribute.getDefaultValue();
            Object aliasedDefaultValue = aliasedAttribute.getDefaultValue();

            if (defaultValue == null || aliasedDefaultValue == null) {
                String msg = String.format("Misconfigured aliases: attribute '%s' in annotation [%s] " +
                                "and attribute '%s' in annotation [%s] must declare default values.",
                        this.sourceAttributeName, this.sourceAnnotationType.getName(), aliasedAttribute.getName(),
                        aliasedAttribute.getDeclaringClass().getName());
                throw new AnnotationConfigurationException(msg);
            }

            if (!ObjectUtils.nullSafeEquals(defaultValue, aliasedDefaultValue)) {
                String msg = String.format("Misconfigured aliases: attribute '%s' in annotation [%s] " +
                                "and attribute '%s' in annotation [%s] must declare the same default value.",
                        this.sourceAttributeName, this.sourceAnnotationType.getName(), aliasedAttribute.getName(),
                        aliasedAttribute.getDeclaringClass().getName());
                throw new AnnotationConfigurationException(msg);
            }
        }

        /**
         * Validate this descriptor against the supplied descriptor.
         * <p>This method only validates the configuration of default values
         * for the two descriptors, since other aspects of the descriptors
         * are validated when they are created.
         */
        private void validateAgainst(AliasDescriptor otherDescriptor) {
            validateDefaultValueConfiguration(otherDescriptor.sourceAttribute);
        }

        /**
         * Determine if this descriptor represents an explicit override for
         * an attribute in the supplied {@code metaAnnotationType}.
         * @see #isAliasFor
         */
        private boolean isOverrideFor(Class<? extends Annotation> metaAnnotationType) {
            return (this.aliasedAnnotationType == metaAnnotationType);
        }

        /**
         * Determine if this descriptor and the supplied descriptor both
         * effectively represent aliases for the same attribute in the same
         * target annotation, either explicitly or implicitly.
         * <p>This method searches the attribute override hierarchy, beginning
         * with this descriptor, in order to detect implicit and transitively
         * implicit aliases.
         * @return {@code true} if this descriptor and the supplied descriptor
         * effectively alias the same annotation attribute
         * @see #isOverrideFor
         */
        private boolean isAliasFor(AliasDescriptor otherDescriptor) {
            for (AliasDescriptor lhs = this; lhs != null; lhs = lhs.getAttributeOverrideDescriptor()) {
                for (AliasDescriptor rhs = otherDescriptor; rhs != null; rhs = rhs.getAttributeOverrideDescriptor()) {
                    if (lhs.aliasedAttribute.equals(rhs.aliasedAttribute)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public List<String> getAttributeAliasNames() {
            // Explicit alias pair?
            if (this.isAliasPair) {
                return Collections.singletonList(this.aliasedAttributeName);
            }

            // Else: search for implicit aliases
            List<String> aliases = new ArrayList<>();
            for (AliasDescriptor otherDescriptor : getOtherDescriptors()) {
                if (this.isAliasFor(otherDescriptor)) {
                    this.validateAgainst(otherDescriptor);
                    aliases.add(otherDescriptor.sourceAttributeName);
                }
            }
            return aliases;
        }

        private List<AliasDescriptor> getOtherDescriptors() {
            List<AliasDescriptor> otherDescriptors = new ArrayList<>();
            for (Method currentAttribute : getAttributeMethods(this.sourceAnnotationType)) {
                if (!this.sourceAttribute.equals(currentAttribute)) {
                    AliasDescriptor otherDescriptor = AliasDescriptor.from(currentAttribute);
                    if (otherDescriptor != null) {
                        otherDescriptors.add(otherDescriptor);
                    }
                }
            }
            return otherDescriptors;
        }

        @Nullable
        public String getAttributeOverrideName(Class<? extends Annotation> metaAnnotationType) {
            Assert.notNull(metaAnnotationType, "metaAnnotationType must not be null");
            Assert.isTrue(Annotation.class != metaAnnotationType,
                    "[java.lang.annotation.Annotation]必须不是metaAnnotationType");

            // Search the attribute override hierarchy, starting with the current attribute
            for (AliasDescriptor desc = this; desc != null; desc = desc.getAttributeOverrideDescriptor()) {
                if (desc.isOverrideFor(metaAnnotationType)) {
                    return desc.aliasedAttributeName;
                }
            }

            // Else: explicit attribute override for a different meta-annotation
            return null;
        }

        @Nullable
        private AliasDescriptor getAttributeOverrideDescriptor() {
            if (this.isAliasPair) {
                return null;
            }
            return AliasDescriptor.from(this.aliasedAttribute);
        }

        /**
         * 获取源属性上提供的@AliasFor注释配置的别名属性，如果未指定别名，则获取原始属性的名称
         * 此方法返回@AliasFor的attribute或value属性的值，以确保仅声明了一个属性，同时确保至少声明了一个属性。
         * @param aliasFor 用于检索别名属性名的@AliasFor注释
         * @param attribute the attribute that is annotated with {@code @AliasFor}
         * @return the name of the aliased attribute (never {@code null} or empty)
         * @throws AnnotationConfigurationException if invalid configuration of
         * {@code @AliasFor} is detected
         */
        private String getAliasedAttributeName(AliasFor aliasFor, Method attribute) {
            //获取属性的名称
            String attributeName = aliasFor.name();
            //获取属性值
            String value = aliasFor.value();
            boolean attributeDeclared = StringUtils.hasText(attributeName);
            boolean valueDeclared = StringUtils.hasText(value);

            // 用户未在@AliasFor中声明“值”和“属性”
            if (attributeDeclared && valueDeclared) {
                String msg = String.format("在注释[%s]中的属性'%s'上声明的@AliasFor中，" +
                                "属性'attribute'及其别名'value'的值分别为[%s]和[%s]，但仅允许有一个。",
                        attribute.getName(), attribute.getDeclaringClass().getName(), attributeName, value);
                throw new AnnotationConfigurationException(msg);
            }

            //默认情况下，要么显式属性名，要么指向同名属性
            attributeName = (attributeDeclared ? attributeName : value);
            //当value和attributeName都没有填写的时候，使用attribute的名称
            return (StringUtils.hasText(attributeName) ? attributeName.trim() : attribute.getName());
        }

        @Override
        public String toString() {
            return String.format("%s: @%s(%s)是 @%s(%s)的别名", getClass().getSimpleName(),
                    this.sourceAnnotationType.getSimpleName(), this.sourceAttributeName,
                    this.aliasedAnnotationType.getSimpleName(), this.aliasedAttributeName);
        }
    }


    private static final class AnnotationCacheKey implements Comparable<AnnotationCacheKey> {

        private final AnnotatedElement element;

        private final Class<? extends Annotation> annotationType;

        public AnnotationCacheKey(AnnotatedElement element, Class<? extends Annotation> annotationType) {
            this.element = element;
            this.annotationType = annotationType;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof AnnotationCacheKey)) {
                return false;
            }
            AnnotationCacheKey otherKey = (AnnotationCacheKey) other;
            return (this.element.equals(otherKey.element) && this.annotationType.equals(otherKey.annotationType));
        }

        @Override
        public int hashCode() {
            return (this.element.hashCode() * 29 + this.annotationType.hashCode());
        }

        @Override
        public String toString() {
            return "@" + this.annotationType + " on " + this.element;
        }

        @Override
        public int compareTo(AnnotationCacheKey other) {
            int result = this.element.toString().compareTo(other.element.toString());
            if (result == 0) {
                result = this.annotationType.getName().compareTo(other.annotationType.getName());
            }
            return result;
        }
    }




}


