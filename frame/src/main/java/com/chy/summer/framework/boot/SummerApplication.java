package com.chy.summer.framework.boot;

import com.chy.summer.framework.beans.BeanUtils;
import com.chy.summer.framework.beans.config.BeanDefinitionRegistry;
import com.chy.summer.framework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import com.chy.summer.framework.boot.ansi.AnsiColor;
import com.chy.summer.framework.boot.ansi.AnsiOutput;
import com.chy.summer.framework.boot.ansi.AnsiStyle;
import com.chy.summer.framework.boot.listeners.SummerApplicationRunListener;
import com.chy.summer.framework.boot.listeners.SummerApplicationRunListeners;
import com.chy.summer.framework.context.ApplicationContext;
import com.chy.summer.framework.context.ApplicationContextInitializer;
import com.chy.summer.framework.context.ConfigurableApplicationContext;
import com.chy.summer.framework.context.annotation.ConfigurationClassPostProcessor;
import com.chy.summer.framework.context.event.ApplicationListener;
import com.chy.summer.framework.context.support.AbstractApplicationContext;
import com.chy.summer.framework.core.GenericTypeResolver;
import com.chy.summer.framework.core.evn.ConfigurableEnvironment;
import com.chy.summer.framework.core.evn.propertysource.*;
import com.chy.summer.framework.core.io.ResourceLoader;
import com.chy.summer.framework.core.io.support.SummerFactoriesLoader;
import com.chy.summer.framework.core.ordered.AnnotationAwareOrderComparator;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.CollectionUtils;
import com.chy.summer.framework.web.servlet.context.support.StandardServletEnvironment;
import com.google.common.base.Stopwatch;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SummerApplication {

    private boolean addConversionService = true;

    @Getter
    private final ResourceLoader resourceLoader;

    private final LinkedHashSet<Class<?>> primarySources;
    //web容器的类型
    private final WebApplicationType webApplicationType;

    @Getter
    private List<ApplicationListener<?>> listeners;

    private ArrayList<ApplicationContextInitializer> initializers;

    //main函数所在的class
    private Class<?> mainApplicationClass;

    private ConfigurableEnvironment environment;

    private Banner.Mode bannerMode = Banner.Mode.CONSOLE;

    //applicationContext 的class,根据webApplicationType 选择而来,这里默认是用 AnnotationConfigServletWebServerApplicationContext
    private Class<? extends ConfigurableApplicationContext> applicationContextClass;

    //默认的web 容器
    private static final String DEFAULT_WEB_CONTEXT_CLASS = "com.chy.summer.framework.web.servlet.AnnotationConfigServletWebServerApplicationContext";

    /**
     * 默认的配置文件项
     * 可以通过设置这个默认的配置项来在启动summer容器的时候来动态改变一些配置文件的值
     *
     */

    @Setter
    private Map<String, Object> defaultProperties;

    /**
     * 设置是否要把 java 启动时候传入的参数当做配置项
     */
    @Setter
    private boolean addCommandLineProperties = true;


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
    private static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args) {
        return new SummerApplication(primarySources).run(args);
    }



    public ConfigurableApplicationContext run(String... args) {
        //开一个秒表,这里用guava的
        Stopwatch stopWatch = Stopwatch.createStarted();
        ConfigurableApplicationContext context = null;
        //Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();

        SummerApplicationRunListeners listeners = getRunListeners(args);
        //所有的监听器开始执行
        //这里执行的监听器 实际上是 EventPublishingRunListener
        //然后在EventPublishingRunListener 触发了事件 -> ApplicationStartingEvent
        listeners.starting();
        try {
            ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
            //对整个容器的环境做一些处理(包括读取配置文件,读取jvm参数)
            ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
            //打印 Banner
            printBanner(environment);
            //把 applicationContext 给实例化完成了,这里默认用 AnnotationConfigServletWebServerApplicationContext
            context = createApplicationContext();
            //对context 做一些前期的配置工作,把入口类给注册进入ioc容器 把一些事先准备好的后置处理器给注册进入IOC容器,为后面的refresh 做准备
            //@see load(ApplicationContext, Set)
            prepareContext(context, environment, listeners, applicationArguments);
            //调用 context的refresh 方法真正开始初始化容器生命周期
            refreshContext(context);
            //扩展点 在刷新容器后做一些其他的事情，暂时没有任何动作
            afterRefresh(context, applicationArguments);
            stopWatch.stop();
            //打印启动时间
            startupInfoLogger(stopWatch);
            //监听器开始
            listeners.started(context);
            //在整个boot项目初始化完成之后干一些事情 一个扩展点
            // 这里会在ioc里找所有的ApplicationRunner CommandLineRunner接口然后执行
            callRunners(context, applicationArguments);
        }
        catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }

        try {
            listeners.running(context);
        }
        catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
        return context;
    }

    private void startupInfoLogger(Stopwatch stopWatch) {
        String text = String.format("================= summer 启动完成 启动耗时为 [%s s] ================="
                ,stopWatch.elapsed(TimeUnit.SECONDS));
        System.out.println(AnsiOutput.toString(AnsiColor.GREEN,text, AnsiStyle.NORMAL));
    }

    private void callRunners(ConfigurableApplicationContext context, ApplicationArguments applicationArguments) {
    }

    private void afterRefresh(ConfigurableApplicationContext context, ApplicationArguments applicationArguments) {
    }

    private void refreshContext(ConfigurableApplicationContext context) {
        refresh(context);
    }

    protected void refresh(ApplicationContext applicationContext) {
        Assert.isInstanceOf(AbstractApplicationContext.class, applicationContext);
        ((AbstractApplicationContext) applicationContext).refresh();
    }

    /**
     * applicationContext 生成完成 后做一些配置
     * 重点关注 load(context, sources)
     * 这个方法会把入口类(main函数所在的类,给注册进入 IOC 容器之中) 同时还会把几个重要的后置处理器给注册进入Ioc 容器
     *
     * @param context
     * @param environment  配置文件的信息会设置进入 application 里面
     * @param listeners    会初始化监听器,会把application 对象给设置进入 监听器之中
     * @param applicationArguments  就是main函数上面 传入的自定义参数,这里会把这些参数封装成一个参数,当做单例给注册进去
     */
    private void prepareContext(ConfigurableApplicationContext context,
                                ConfigurableEnvironment environment, SummerApplicationRunListeners listeners,
                                ApplicationArguments applicationArguments) {
        context.setEnvironment(environment);
        postProcessApplicationContext(context);
        //在这里会调用 ApplicationContextInitializer 接口的 initializer 方法
        applyInitializers(context);
        //这里会调用 SummerApplicationRunListener 接口的 contextPrepared 方法来初始化 监听器
        listeners.contextPrepared(context);
        // 把applicationArguments 当做单例对象给放入ioc 容器中
        context.getBeanFactory().registerSingleton("springApplicationArguments", applicationArguments);
        Set<Object> sources = getAllSources();
        Assert.notEmpty(sources, "Sources 不能为空");
        //这里会把 sources(入口类) 注册进ioc容器
        load(context, sources);
        //向所有监听器注入 context对象
        listeners.contextLoaded(context);
    }

    /**
     * 入口类的解析,在boot项目里 其实就是那个 main函数所在的类
     * 这里解析了入口类之后,会把他生成 beanDefinition 然后注册进入ioc里面
     *
     * 同时 还会注册几个后置处理器,用来真正扫描整个项目,注册后置处理器的位置在
     * new BeanDefinitionLoader() --> new AnnotatedBeanDefinitionReader(registry) --> AnnotationUtils.registerAnnotationConfigProcessors(registry,null)
     * @see com.chy.summer.framework.core.annotation.AnnotationUtils#registerAnnotationConfigProcessors(BeanDefinitionRegistry, Object)
     * 注册进入的 postProcessor 为:
     *      @see ConfigurationClassPostProcessor    用了解析入口类,然后扫描项目里的所有业务类,注册进Ioc中
     *      @see AutowiredAnnotationBeanPostProcessor  用来生成对象之后注入属性的值
     *
     * @param context applicationContext 主要就是用了用他去获取 DefaultListableBeanFactory
     * @param  sources 要解析的入口对象,一般就是 main函数所在的类
     *
     **/
    protected void load(ApplicationContext context, Set<Object> sources) {
        log.debug("开始加载 source : [%s]",sources);

        //这里就是去 applicationContext 里拿 DefaultListableBeanFactory
        BeanDefinitionRegistry beanDefinitionRegistry = getBeanDefinitionRegistry(context);
        //beanDefintion 的装载器, 这个装载器里在原本的spring里面有各种 读取器 比如 AnnotatedBeanDefinitionReader , XmlBeanDefinitionReader
        //这里只考虑 注解的加载方式,所以只有 AnnotatedBeanDefinitionReader
        BeanDefinitionLoader loader = new BeanDefinitionLoader(beanDefinitionRegistry, sources);

        if (this.resourceLoader != null) {
            loader.setResourceLoader(this.resourceLoader);
        }
        //上面都是前戏,这里开始干活
        loader.load();
    }

    private BeanDefinitionRegistry getBeanDefinitionRegistry(ApplicationContext context) {
        if (context instanceof BeanDefinitionRegistry) {
            return (BeanDefinitionRegistry) context;
        }
        if (context instanceof AbstractApplicationContext) {
            return (BeanDefinitionRegistry) ((AbstractApplicationContext) context)
                    .getBeanFactory();
        }
        throw new IllegalStateException("Could not locate BeanDefinitionRegistry");
    }


    public Set<Object> getAllSources() {
        Set<Object> allSources = new LinkedHashSet<>();
        if (!CollectionUtils.isEmpty(this.primarySources)) {
            allSources.addAll(this.primarySources);
        }
        return allSources;
    }

    protected void postProcessApplicationContext(ConfigurableApplicationContext context) {
        //TODO applicationContext 的前置处理器,设置一些自定义的东西
    }

    /**
     * 找到所有的 ApplicationContextInitializer 接口,然后调用他的 initialize 方法来初始化容器1
     * @param context
     */
    protected void applyInitializers(ConfigurableApplicationContext context) {
        for (ApplicationContextInitializer initializer : getInitializers()) {
            Class<?> requiredType = GenericTypeResolver.resolveTypeArgument(
                    initializer.getClass(), ApplicationContextInitializer.class);
            Assert.isInstanceOf(requiredType, context, requiredType+" initializer 的类型不对.");
            initializer.initialize(context);
        }
    }



    protected ConfigurableApplicationContext createApplicationContext() {
        Class<?> contextClass = this.applicationContextClass;
        if (contextClass == null) {
            try {
                switch (this.webApplicationType) {
                    case SERVLET:
                        contextClass = Class.forName(DEFAULT_WEB_CONTEXT_CLASS);
                        break;
                    default:
                        contextClass = Class.forName(DEFAULT_WEB_CONTEXT_CLASS);
                }
            }
            catch (ClassNotFoundException ex) {
                throw new IllegalStateException(
                        "Unable create a default ApplicationContext, "
                                + "please specify an ApplicationContextClass",
                        ex);
            }
        }
        return (ConfigurableApplicationContext) BeanUtils.instantiateClass(contextClass);
    }


    private void printBanner(ConfigurableEnvironment environment) {
        SummerBootBanner summerBootBanner = new SummerBootBanner();
        summerBootBanner.printBanner(environment, this.mainApplicationClass, System.out);
    }


    private ConfigurableEnvironment prepareEnvironment(SummerApplicationRunListeners listeners,
                                                ApplicationArguments applicationArguments) {

        //通过 webApplicationType 的类型去创建不同的环境
        ConfigurableEnvironment environment = getOrCreateEnvironment();
        //对刚创建的 环境对象做一些参数的配置
        configureEnvironment(environment, applicationArguments.getSourceArgs());
        //监听器触发了 environmentPrepared 事件
        //就是在这次事件中去加载了 用户自定义的 application.yaml 文件
        listeners.environmentPrepared(environment);
        //绑定环境对象
        //bindToSpringApplication(environment);

        //下面还有自定义的环境处理这里先忽略
        return environment;
    }

    protected void configureEnvironment(ConfigurableEnvironment environment,
                                        String[] args) {
        if (this.addConversionService) {
            //TODO 类型转换器后面再补吧
            /*ConversionService conversionService = ApplicationConversionService
                    .getSharedInstance();
            environment.setConversionService(
                    (ConfigurableConversionService) conversionService);*/
        }
        //去设置一些默认的配置属性
        configurePropertySources(environment, args);
        //TODO configureProfiles(environment, args);
    }


    /**
     * 去设置一些默认的配置属性
     * 1. 直接调用summer容器设置进去的属性
     * 2. 启动的时候通过命令行加入的属性
     *
     * @param environment
     * @param args
     */
    protected void configurePropertySources(ConfigurableEnvironment environment,
                                            String[] args) {
        //拿到所有的配置文件项
        //执行到这里的时候 应该只 systemProperties systemEnvironment 以及 servlet 相关的配置项
        MutablePropertySources sources = environment.getPropertySources();
        //如果设置了默认的属性,那么把手动设置的属性给放进去
        if (this.defaultProperties != null && !this.defaultProperties.isEmpty()) {
            sources.addLast(new MapPropertySource("defaultProperties", this.defaultProperties));
        }

        //如果从命令行传入了参数,那么从这里给塞进去
        if (this.addCommandLineProperties && args.length > 0) {
            sources.addFirst(new SimpleCommandLinePropertySource(args));
        }
    }


    /**
     * 去获取 环境对象
     * 环境对象是用了存放所有 配置属性的 容器对象
     * @return
     */
    private ConfigurableEnvironment getOrCreateEnvironment() {
        if (this.environment != null) {
            return this.environment;
        }
        //这里根据 webApplicationType 的类型去创建不同的环境
        switch (this.webApplicationType) {
            case SERVLET:
                return new StandardServletEnvironment();
            default:
                return new StandardServletEnvironment();
        }
    }


    private SummerApplicationRunListeners getRunListeners(String[] args) {
        Class<?>[] types = new Class<?>[] { SummerApplication.class, String[].class };
        //这里去 summer.factories 文件里拿到所有类型为 SummerApplicationRunListener 的类,然后实例化
        Collection<SummerApplicationRunListener> summerFactoriesInstances =
                getSummerFactoriesInstances(SummerApplicationRunListener.class, types, this, args);
        return new SummerApplicationRunListeners(summerFactoriesInstances);
    }

    public ArrayList<ApplicationContextInitializer> getInitializers() {
        return initializers;
    }


}
