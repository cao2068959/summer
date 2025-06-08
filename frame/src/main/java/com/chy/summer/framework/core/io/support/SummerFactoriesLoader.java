package com.chy.summer.framework.core.io.support;

import com.chy.summer.framework.core.io.UrlResource;
import com.chy.summer.framework.core.ordered.AnnotationAwareOrderComparator;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.ReflectionUtils;
import com.chy.summer.framework.util.StringUtils;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class SummerFactoriesLoader {


    public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/summer.factories";

    private static final Map<ClassLoader, Map<String, List<String>>> cache = new ConcurrentHashMap<>();

    /**
     * 加载解析 META-INF/summer.factories 文件
     * @param classLoader
     * @return
     */
    private static Map<String, List<String>> loadSummerFactories(@Nullable ClassLoader classLoader) {
        Map<String, List<String>> result = cache.get(classLoader);
        if (result != null) {
            return result;
        }

        try {
            Enumeration<URL> urls = (classLoader != null ?
                    classLoader.getResources(FACTORIES_RESOURCE_LOCATION) :
                    ClassLoader.getSystemResources(FACTORIES_RESOURCE_LOCATION));
            result = new HashMap<>();
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                UrlResource resource = new UrlResource(url);
                Properties properties = PropertiesLoaderUtils.loadProperties(resource);
                for (Map.Entry<?, ?> entry : properties.entrySet()) {
                    //summer.factories 里的数据都是 key = A,B,C,D 这样的 这里变成 [A,B,C,D] 然后放入到 list 里面
                    List<String> factoryClassNames = Arrays.asList(
                            StringUtils.commaDelimitedListToStringArray((String) entry.getValue()));
                    result.put((String) entry.getKey(), factoryClassNames);
                }
            }
            cache.put(classLoader, result);
            return result;
        }
        catch (IOException ex) {
            throw new IllegalArgumentException("不能够加载 factories 文件 [" +
                    FACTORIES_RESOURCE_LOCATION + "]", ex);
        }
    }


    /**
     * 根据 传入的类型 去 META-INF/summer.factories 文件 获取对应的 目标类的全路径
     * @param factoryClass
     * @param classLoader
     * @return
     */
    public static List<String> loadFactoryNames(Class<?> factoryClass, @Nullable ClassLoader classLoader) {
        String factoryClassName = factoryClass.getName();
        return loadSummerFactories(classLoader).getOrDefault(factoryClassName, Collections.emptyList());
    }


    /**
     *  同上面 loadFactoryNames , 这里
     * @param factoryType
     * @param classLoader
     * @param <T>
     * @return
     */
    public static <T> List<T> loadFactories(Class<T> factoryType, @Nullable ClassLoader classLoader) {
        Assert.notNull(factoryType, "'factoryType' must not be null");
        ClassLoader classLoaderToUse = classLoader;
        //如果没传入类加载器就直接用这个加载这个类的类加载器
        if (classLoaderToUse == null) {
            classLoaderToUse = SummerFactoriesLoader.class.getClassLoader();
        }
        //去 summer.factories 拿到目标类的 全路径
        List<String> factoryImplementationNames = loadFactoryNames(factoryType, classLoaderToUse);
        log.trace("加载了类型: [{}] ,加载到的目标为 : [{}]",factoryType.getName(),factoryImplementationNames);
        List<T> result = new ArrayList<>(factoryImplementationNames.size());
        //上面拿到的是 类的全路径,现在要开始实例化对象了
        for (String factoryImplementationName : factoryImplementationNames) {
            result.add(instantiateFactory(factoryImplementationName, factoryType, classLoaderToUse));
        }
        //最后排序一波
        AnnotationAwareOrderComparator.sort(result);
        return result;
    }


    /**
     * 用了实例化 对象的
     * @param factoryImplementationName
     * @param factoryType
     * @param classLoader
     * @param <T>
     * @return
     */
    private static <T> T instantiateFactory(String factoryImplementationName, Class<T> factoryType, ClassLoader classLoader) {
        try {
            Class<?> factoryImplementationClass = ClassUtils.forName(factoryImplementationName, classLoader);
            if (!factoryType.isAssignableFrom(factoryImplementationClass)) {
                throw new IllegalArgumentException(
                        "Class [" + factoryImplementationName + "] is not assignable to factory type [" + factoryType.getName() + "]");
            }
            return (T) ReflectionUtils.accessibleConstructor(factoryImplementationClass).newInstance();
        }
        catch (Throwable ex) {
            throw new IllegalArgumentException(
                    "Unable to instantiate factory class [" + factoryImplementationName + "] for factory type [" + factoryType.getName() + "]",
                    ex);
        }
    }

}
