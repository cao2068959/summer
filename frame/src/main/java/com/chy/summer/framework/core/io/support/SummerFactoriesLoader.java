package com.chy.summer.framework.core.io.support;

import com.chy.summer.framework.core.io.UrlResource;
import com.chy.summer.framework.core.ordered.AnnotationAwareOrderComparator;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.StringUtils;
import com.sun.istack.internal.Nullable;
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


    public static List<String> loadFactoryNames(Class<?> factoryClass, @Nullable ClassLoader classLoader) {
        String factoryClassName = factoryClass.getName();
        return loadSummerFactories(classLoader).getOrDefault(factoryClassName, Collections.emptyList());
    }

}
