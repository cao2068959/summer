package com.chy.summer.framework.boot;

import com.chy.summer.framework.beans.BeanUtils;
import com.chy.summer.framework.context.ApplicationContext;
import com.chy.summer.framework.context.ApplicationContextInitializer;
import com.chy.summer.framework.context.event.ApplicationListener;
import com.chy.summer.framework.core.io.ResourceLoader;
import com.chy.summer.framework.core.io.support.SummerFactoriesLoader;
import com.chy.summer.framework.core.ordered.AnnotationAwareOrderComparator;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.util.*;

public class SummerApplication {


    private final ResourceLoader resourceLoader;

    private final LinkedHashSet<Class<?>> primarySources;
    //web容器的类型
    private final WebApplicationType webApplicationType;

    private List<ApplicationListener<?>> listeners;

    private ArrayList<Object> initializers;

    //main函数所在的class
    private Class<?> mainApplicationClass;


    public SummerApplication(Class<?>... primarySources) {
        this(null,primarySources);
    }


    public SummerApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
        this.resourceLoader = resourceLoader;
        Assert.notNull(primarySources, "primarySources 不能为空");
        this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
        //选择web 容器的类型
        this.webApplicationType = deduceWebApplicationType();
        //这里从 META-INF/summer.factories 文件 拿到 ApplicationContextInitializer 为key 后面的所有类,实例化后 放入 initializers
        setInitializers((Collection) getSummerFactoriesInstances(ApplicationContextInitializer.class,null,null));
        //这里从 META-INF/summer.factories 文件 拿到 ApplicationListener 为key 后面的所有类,实例化后 放入 listeners
        setListeners((Collection) getSummerFactoriesInstances(ApplicationListener.class,null,null));
        //通过异常信息来寻找main函数所在的类
        this.mainApplicationClass = deduceMainApplicationClass();
    }

    public void setListeners(Collection<? extends ApplicationListener<?>> listeners) {
        this.listeners = new ArrayList<>();
        this.listeners.addAll(listeners);
    }

    private Class<?> deduceMainApplicationClass() {
        try {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if ("main".equals(stackTraceElement.getMethodName())) {
                    return Class.forName(stackTraceElement.getClassName());
                }
            }
        }
        catch (ClassNotFoundException ex) {
        }
        return null;
    }

    private <T> Collection<T> getSummerFactoriesInstances(Class<T> type,
                                                          Class<?>[] parameterTypes, Object... args) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        //这里拿到了 在META-INF/summer.factories 文件里 对应类型 的所有类的 name
        //这里传入classLoader 进去是因为 这里用 classLoader为key 做了缓存
        List<String> namesRe = SummerFactoriesLoader.loadFactoryNames(type, classLoader);
        //用户可能会写了重复的 类的名字,这里去重
        Set<String> names = new LinkedHashSet<>(namesRe);
        //把上面写了的名字 全部实例化了
        List<T> instances = createSummerFactoriesInstances(type, parameterTypes,
                classLoader, args, names);
        //排序
        AnnotationAwareOrderComparator.sort(instances);
        return instances;
    }

    private <T> List<T> createSummerFactoriesInstances(Class<T> type,
                                                       Class<?>[] parameterTypes, ClassLoader classLoader, Object[] args,
                                                       Set<String> names) {
        List<T> instances = new ArrayList<>(names.size());
        for (String name : names) {
            try {
                //反射拿到类
                Class<?> instanceClass = ClassUtils.forName(name, classLoader);
                //拿到构造器
                Constructor<?> constructor = instanceClass.getDeclaredConstructor(parameterTypes);

                T instance = (T) BeanUtils.instantiateClass(constructor, args);
                instances.add(instance);
            }
            catch (Throwable ex) {
                throw new IllegalArgumentException(
                        "Cannot instantiate " + type + " : " + name, ex);
            }
        }
        return instances;
    }


    /**
     * 用了选择 web容器
     * @return
     */
    private WebApplicationType deduceWebApplicationType() {
        //spring 里面是通过判断 几个类是否存在来 选择容器类型.
        //这里考虑用传参的方式来选择?
        //这里默认就是 SERVLET
        return WebApplicationType.SERVLET;
    }

    /**
     *
     *
     * 把 实现了 ApplicationContextInitializerd 的类给保存一下
     * @param initializers
     */
    public void setInitializers(
            Collection<? extends ApplicationContextInitializer<?>> initializers) {
        this.initializers = new ArrayList<>();
        this.initializers.addAll(initializers);
    }


    /**
     * 启动入口
     * @param primarySource
     * @param args
     * @return
     */
    public static ApplicationContext run(Class<?> primarySource,
                                         String... args) {
        return run(new Class<?>[] { primarySource }, args);
    }

    /**
     * 同上
     * @param primarySources
     * @return
     */
    private static ApplicationContext run(Class<?>[] primarySources, String[] args) {
        return new SummerApplication(primarySources).run(args);
    }



    private ApplicationContext run(String[] args) {


        return null;
    }


}
