package com.chy.summer.framework.boot.context.config;


import com.chy.summer.framework.boot.SummerApplication;
import com.chy.summer.framework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import com.chy.summer.framework.boot.context.event.SmartApplicationListener;
import com.chy.summer.framework.context.event.ApplicationEvent;
import com.chy.summer.framework.core.evn.ConfigurableEnvironment;
import com.chy.summer.framework.core.evn.EnvironmentPostProcessor;
import com.chy.summer.framework.core.evn.PropertySourceLoader;
import com.chy.summer.framework.core.evn.propertysource.MutablePropertySources;
import com.chy.summer.framework.core.evn.propertysource.PropertySource;
import com.chy.summer.framework.core.io.DefaultResourceLoader;
import com.chy.summer.framework.core.io.ResourceLoader;
import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.core.io.support.SummerFactoriesLoader;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.CollectionUtils;
import com.chy.summer.framework.util.ObjectUtils;
import com.chy.summer.framework.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


/**
 * 配置文件的 事件监听处理器, 当响应ApplicationEnvironmentPreparedEvent 事件的时候会去 加载配置文件
 */
@Slf4j
public class ConfigFileApplicationListener implements SmartApplicationListener, EnvironmentPostProcessor {


    private static final String DEFAULT_SEARCH_LOCATIONS = "classpath:/,classpath:/config/,file:./,file:./config/";
    /**
     * 用了设置配置文件的名字 默认叫做 appliction
     */
    private static final String CONFIG_NAME_PROPERTY = "summer.config.name";
    private static final String DEFAULT_NAMES = "appliction";


    private static final Set<String> NO_SEARCH_NAMES = Collections.singleton(null);

    private static final String DEFAULT_PROPERTIES = "defaultProperties";
    /**
     * 设置配置文件的加载路径,如果是null使用 DEFAULT_SEARCH_LOCATIONS
     */
    @Setter
    private String searchLocations;

    /**
     * 用了设置配置文件的名称
     */
    @Setter
    private String names;

    /**
     * 通过事件类型去匹配是否是同一个事件类型
     *
     * @param eventType
     * @return
     */
    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return ApplicationEnvironmentPreparedEvent.class.isAssignableFrom(eventType);
    }


    /**
     * 响应了事件执行,也是事件的入口
     *
     * @param event
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        //如果是 ApplicationEnvironmentPreparedEvent 事件那么执行 加载配置文件的逻辑
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            onApplicationEnvironmentPreparedEvent((ApplicationEnvironmentPreparedEvent) event);
        }
    }

    /**
     * 响应 ApplicationEnvironmentPreparedEvent 事件的逻辑
     * 会执行所有的 EnvironmentPostProcessor 后置处理器
     *
     * @param event
     */
    private void onApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent event) {
        //去 summer.factories 文件里 获取 EnvironmentPostProcessor 接口的类
        List<EnvironmentPostProcessor> postProcessors = loadPostProcessors();
        //这个类本身也是一个  EnvironmentPostProcessor 类型,把他放在最后的位置
        postProcessors.add(this);
        //去执行了所有的 EnvironmentPostProcessor 的后置处理
        for (EnvironmentPostProcessor postProcessor : postProcessors) {
            postProcessor.postProcessEnvironment(event.getEnvironment(), event.getSummerApplication());
        }

    }

    List<EnvironmentPostProcessor> loadPostProcessors() {
        return SummerFactoriesLoader.loadFactories(EnvironmentPostProcessor.class, getClass().getClassLoader());
    }

    /**
     * 这个类本身就是一个 EnvironmentPostProcessor 后置处理器, 执行 EnvironmentPostProcessor 应有的逻辑
     *
     * @param environment
     * @param application
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SummerApplication application) {
        addPropertySources(environment, application.getResourceLoader());
    }

    /**
     * @param environment
     * @param resourceLoader
     */
    private void addPropertySources(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
        new Loader(environment, resourceLoader).load();
    }


    /**
     * 真正去加载了配置文件的类
     */
    private class Loader {

        private final ConfigurableEnvironment environment;

        private final ResourceLoader resourceLoader;

        private Deque<Profile> profiles;

        private List<Profile> processedProfiles;

        private boolean activatedProfiles;

        private Map<Profile, MutablePropertySources> loaded;

        /**
         * 配置文件加载器,这里默认的实现是 property文件加载器以及 yml 文件加载器
         */
        private final List<PropertySourceLoader> propertySourceLoaders;

        /**
         * 配置文件的缓存类
         */
        private Map<DocumentsCacheKey, List<Document>> loadDocumentsCache = new HashMap<>();

        Loader(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
            this.environment = environment;
            //this.placeholdersResolver = new PropertySourcesPlaceholdersResolver(this.environment);
            this.resourceLoader = (resourceLoader != null) ? resourceLoader : new DefaultResourceLoader();
            //去summer.factories 文件中 获取所有的 PropertySourceLoader 实现对象
            this.propertySourceLoaders = SummerFactoriesLoader.loadFactories(PropertySourceLoader.class,
                    getClass().getClassLoader());
        }

        /**
         * 外部调用加载配置文件的入口
         */
        protected void load() {
            loadLocalProfile();
        }

        /**
         * 去加载本地的配置文件
         */
        protected void loadLocalProfile() {
            this.profiles = new LinkedList<>();
            this.processedProfiles = new LinkedList<>();
            this.activatedProfiles = false;
            this.loaded = new LinkedHashMap<>();
            //去把默认的 profile 放入容器
            //如果在 jvm参数中指定了 profiles.active 参数会把对应的值给放入 profiles,否则就在profiles 里放入 default
            initializeProfiles();
            while (!this.profiles.isEmpty()) {
                //从 profiles 里拿出一个去处理
                Profile profile = this.profiles.poll();
                //判断这个 profile 是不是默认值
                if (isDefaultProfile(profile)) {
                    addProfileToEnvironment(profile.getName());
                }
                //去加载配置文件
                load(profile, this::getPositiveProfileFilter, addToLoaded(MutablePropertySources::addLast, false));
                this.processedProfiles.add(profile);
            }

            //把解析了的配置文件的值(存this.loaded 里的) 放入 env 对象里
            addLoadedPropertySources();

            //把解析到的配置文件变成 活跃状态
            applyActiveProfiles();
        }

        /**
         * 把解析了的配置文件的值(存this.loaded 里的) 放入 env 对象里
         */
        private void addLoadedPropertySources() {
            MutablePropertySources destination = this.environment.getPropertySources();
            List<MutablePropertySources> loaded = new ArrayList<>(this.loaded.values());
            Collections.reverse(loaded);
            String lastAdded = null;
            Set<String> added = new HashSet<>();
            for (MutablePropertySources sources : loaded) {
                for (PropertySource<?> source : sources) {
                    if (added.add(source.getName())) {
                        addLoadedPropertySource(destination, lastAdded, source);
                        lastAdded = source.getName();
                    }
                }
            }
        }

        /**
         * 用来控制 配置文件的插入优先级的
         * @param destination
         * @param lastAdded
         * @param source
         */
        private void addLoadedPropertySource(MutablePropertySources destination, String lastAdded,
                                             PropertySource<?> source) {
            if (lastAdded == null) {
                if (destination.contains(DEFAULT_PROPERTIES)) {
                    destination.addBefore(DEFAULT_PROPERTIES, source);
                    return;
                }
                destination.addLast(source);
                return;
            }

            destination.addAfter(lastAdded, source);
        }

        /**
         * 把解析到的配置文件变成 活跃状态
         */
        private void applyActiveProfiles() {
            List<String> activeProfiles = new ArrayList<>();

            this.processedProfiles.stream().filter(this::isDefaultProfile).map(Profile::getName)
                    .forEach(activeProfiles::add);
            this.environment.setActiveProfiles(activeProfiles.toArray(new String[0]));
        }

        private DocumentFilter getPositiveProfileFilter(Profile profile) {
            return (Document document) -> {
                if (profile == null) {
                    return ObjectUtils.isEmpty(document.getProfiles());
                }
                //判断一下 是否是当前需要激活的 profile
                return ObjectUtils.containsElement(document.getProfiles(), profile.getName());
            };
        }

        /**
         * 把解析出来的配置文件的值塞入 loaded 容器里
         *
         * @param addMethod
         * @param checkForExisting
         * @return
         */
        private DocumentConsumer addToLoaded(BiConsumer<MutablePropertySources, PropertySource<?>> addMethod,
                                             boolean checkForExisting) {
            return (profile, document) -> {
                if (checkForExisting) {
                    for (MutablePropertySources merged : this.loaded.values()) {
                        if (merged.contains(document.getPropertySource().getName())) {
                            return;
                        }
                    }
                }
                //
                MutablePropertySources merged = this.loaded.computeIfAbsent(profile, (k) -> new MutablePropertySources());
                addMethod.accept(merged, document.getPropertySource());
            };
        }

        /**
         * 把 profile 设置进 env里面
         *
         * @param profile
         */
        private void addProfileToEnvironment(String profile) {
            for (String activeProfile : this.environment.getActiveProfiles()) {
                if (activeProfile.equals(profile)) {
                    return;
                }
            }
            this.environment.addActiveProfile(profile);
        }

        /**
         * 把默认的 profile 放入 容器
         */
        private void initializeProfiles() {
            this.profiles.add(null);

            if (this.profiles.size() == 1) {
                for (String defaultProfileName : this.environment.getDefaultProfiles()) {
                    Profile defaultProfile = new Profile(defaultProfileName, true);
                    this.profiles.add(defaultProfile);
                }
            }
        }


        private boolean isDefaultProfile(Profile profile) {
            return profile != null && !profile.isDefaultProfile();
        }


        /**
         * 加载配置文件的流程之一,这里面主要是 查找了要加载的目录,以及要加载的文件的名称
         *
         * @param profile
         * @param filterFactory
         * @param consumer
         */
        private void load(Profile profile, DocumentFilterFactory filterFactory, DocumentConsumer consumer) {
            getSearchLocations().forEach((location) -> {
                //判断是否是文件夹
                boolean isFolder = location.endsWith("/");
                //如果是文件夹,要么要去这个文件夹下面查找的配置文件的名字是什么? 默认是application
                Set<String> names = isFolder ? getSearchNames() : NO_SEARCH_NAMES;
                //下面真的去加载配置文件了
                names.forEach((name) -> load(location, name, profile, filterFactory, consumer));
            });
        }

        /**
         * 拿到了要加载的目录,以及文件的名称,这里就真的要去加载配置文件了
         *
         * @param location
         * @param name
         * @param profile
         * @param filterFactory
         * @param consumer
         */
        private void load(String location, String name, Profile profile, DocumentFilterFactory filterFactory,
                          DocumentConsumer consumer) {

            Set<String> processed = new HashSet<>();
            //遍历属性文件加载器去加载配置文件,默认 property和yml加载器
            for (PropertySourceLoader loader : this.propertySourceLoaders) {
                //用这个文件加载器支持的后缀去解析,其实默认一个加载器只支持一个后缀,不过有的可能有多个,比如 yaml和yml 其实是一种
                for (String fileExtension : loader.getFileExtensions()) {
                    if (processed.add(fileExtension)) {
                        loadForFileExtension(loader, location + name, "." + fileExtension, profile, filterFactory,
                                consumer);
                    }
                }
            }
        }

        /**
         * 去加载配置文件,这方法主要做的是 用prefix把配置文件的名字给拼接了,比如  application-chy.yml
         *
         * @param loader
         * @param prefix
         * @param fileExtension
         * @param profile
         * @param filterFactory
         * @param consumer
         */
        private void loadForFileExtension(PropertySourceLoader loader, String prefix, String fileExtension,
                                          Profile profile, DocumentFilterFactory filterFactory, DocumentConsumer consumer) {


            DocumentFilter profileFilter = filterFactory.getDocumentFilter(profile);

            //profile == null 说明是 application.yml 文件 如果 !=null 说明 是 application-xxx 这样的文件
            if (profile != null) {
                //拼接一下配置文件的名称
                String profileSpecificFile = prefix + "-" + profile + fileExtension;
                //加载 application-{prefix} 文件
                load(loader, profileSpecificFile, profile, profileFilter, consumer);
                //下面是 如果用户自定义了 profiles 那么走下面
                for (Profile processedProfile : this.processedProfiles) {
                    if (processedProfile != null) {
                        String previouslyLoaded = prefix + "-" + processedProfile + fileExtension;
                        load(loader, previouslyLoaded, profile, profileFilter, consumer);
                    }
                }
                return;
            }
            //加载 application.yml 文件
            load(loader, prefix + fileExtension, profile, profileFilter, consumer);
        }

        /**
         * 最后一层的 load 了,真的开始加载配置文件了
         *
         * @param loader
         * @param location  要查找的文件的额全路径 file:./config/appliction.properties
         * @param profile  文件标志位 比如 appliction.properties 就是 null , appliction-dev.properties 就是dev
         * @param filter  当加载了配置文件后,用来判断这个配置文件应该被加入 spring容器吗,其实也就是去判断他有没被当前激活
         * @param consumer
         */
        private void load(PropertySourceLoader loader, String location, Profile profile, DocumentFilter filter,
                          DocumentConsumer consumer) {
            try {
                //去找对应位置的对应文件
                Resource resource = this.resourceLoader.getResource(location);
                //如果没有找到对应的文件,就直接结束方法了
                if (resource == null || !resource.exists()) {
                    log.trace("没有找到配置文件 [{}] ", location);
                    return;
                }
                //获取文件的后缀,如果没有后缀那么也直接跳过了
                if (!StringUtils.hasText(StringUtils.getFilenameExtension(resource.getFilename()))) {
                    log.trace("跳过配置文件解析,原因:后缀不能为null [{}] ", location);
                    return;
                }
                String name = "applicationConfig: [" + location + "]";
                //去读取配置文件里的内容
                List<Document> documents = loadDocuments(loader, name, resource);
                if (CollectionUtils.isEmpty(documents)) {
                    log.trace("没有解析到配置文件的内容 [{}] ", name);
                    return;
                }
                List<Document> loaded = new ArrayList<>();
                for (Document document : documents) {
                    if (filter.match(document)) {
                        //addActiveProfiles(document.getActiveProfiles());
                        //addIncludedProfiles(document.getIncludeProfiles());
                        loaded.add(document);

                    }
                }
                Collections.reverse(loaded);
                if (!loaded.isEmpty()) {
                    loaded.forEach((document) -> {
                        log.trace("加载了配置文件 [{}] ", document);
                        //这里其实就是把 document 加入到 this.loaded 里面
                        consumer.accept(profile, document);
                        return;

                    });
                }
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to load property source from location '" + location + "'", ex);
            }
        }

        /**
         * 读取配置文件的内容
         *
         * @param loader
         * @param name
         * @param resource
         * @return
         * @throws IOException
         */
        private List<Document> loadDocuments(PropertySourceLoader loader, String name, Resource resource)
                throws IOException {
            //生成一个 key
            DocumentsCacheKey cacheKey = new DocumentsCacheKey(loader, resource);
            //去查询一下缓存是否存在
            List<Document> documents = this.loadDocumentsCache.get(cacheKey);
            if (documents == null) {
                //用配置解析器去正真解析配置文件里的内容
                List<PropertySource<?>> loaded = loader.load(name, resource);
                //转成 Document 类型
                documents = asDocuments(loaded);
                this.loadDocumentsCache.put(cacheKey, documents);
            }
            return documents;
        }

        private List<Document> asDocuments(List<PropertySource<?>> loaded) {
            if (loaded == null) {
                return Collections.emptyList();
            }
            //TODO 这里Document 的入参不够
            return loaded.stream().map((propertySource) -> {
                return new Document(propertySource, null, null, null);
            }).collect(Collectors.toList());
        }


        /**
         * 获取配置文件的加载路径
         * 默认是 并且按照顺序有加载优先级
         * file:./config/
         * file:./
         * classpath:/config/
         * classpath:/
         *
         * @return
         */
        private Set<String> getSearchLocations() {
            Set<String> locations = new LinkedHashSet<>();
            locations.addAll(asResolvedSet(ConfigFileApplicationListener.this.searchLocations, DEFAULT_SEARCH_LOCATIONS));
            return locations;
        }

        /**
         * 要加载的配置文件的名字,默认叫做 application
         *
         * @return
         */
        private Set<String> getSearchNames() {
            //去检查一下 有没有从 Jvm参数的传入配置 summer.config.name 来指定配置文件的名称的
            if (this.environment.containsProperty(CONFIG_NAME_PROPERTY)) {
                String property = this.environment.getProperty(CONFIG_NAME_PROPERTY);
                return asResolvedSet(property, null);
            }
            //然后看看 有没从代码层面去设置了配置文件的名字,如果也没有就直接用默认的 叫做 application
            return asResolvedSet(ConfigFileApplicationListener.this.names, DEFAULT_NAMES);
        }

        private Set<String> asResolvedSet(String value, String fallback) {
            List<String> list = Arrays.asList(StringUtils.trimArrayElements(StringUtils.commaDelimitedListToStringArray(
                    (value != null) ? this.environment.resolvePlaceholders(value) : fallback)));
            Collections.reverse(list);
            return new LinkedHashSet<>(list);
        }


    }

    /**
     * 每一个能够加载的配置文件的名字 就是一个 Profile
     * 比如文件 application-chy.yml 他的 profile就是 chy
     */
    private static class Profile {

        @Getter
        private final String name;

        @Getter
        private final boolean defaultProfile;

        Profile(String name) {
            this(name, false);
        }

        Profile(String name, boolean defaultProfile) {
            Assert.notNull(name, "Name must not be null");
            this.name = name;
            this.defaultProfile = defaultProfile;
        }


        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return ((Profile) obj).name.equals(this.name);
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();
        }

        @Override
        public String toString() {
            return this.name;
        }

    }


    @FunctionalInterface
    private interface DocumentFilterFactory {

        DocumentFilter getDocumentFilter(Profile profile);

    }

    private static class Document {

        private final PropertySource<?> propertySource;

        private String[] profiles;

        private final Set<Profile> activeProfiles;

        private final Set<Profile> includeProfiles;

        Document(PropertySource<?> propertySource, String[] profiles, Set<Profile> activeProfiles,
                 Set<Profile> includeProfiles) {
            this.propertySource = propertySource;
            this.profiles = profiles;
            this.activeProfiles = activeProfiles;
            this.includeProfiles = includeProfiles;
        }

        PropertySource<?> getPropertySource() {
            return this.propertySource;
        }

        String[] getProfiles() {
            return this.profiles;
        }

        Set<Profile> getActiveProfiles() {
            return this.activeProfiles;
        }

        Set<Profile> getIncludeProfiles() {
            return this.includeProfiles;
        }

        @Override
        public String toString() {
            return this.propertySource.toString();
        }

    }

    @FunctionalInterface
    private interface DocumentFilter {

        boolean match(Document document);

    }

    @FunctionalInterface
    private interface DocumentConsumer {

        void accept(Profile profile, Document document);

    }

    /**
     * 作为一个缓存的key使用的类,缓存的对象是配置文件
     */
    private static class DocumentsCacheKey {

        private final PropertySourceLoader loader;

        private final Resource resource;

        DocumentsCacheKey(PropertySourceLoader loader, Resource resource) {
            this.loader = loader;
            this.resource = resource;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            DocumentsCacheKey other = (DocumentsCacheKey) obj;
            return this.loader.equals(other.loader) && this.resource.equals(other.resource);
        }

        @Override
        public int hashCode() {
            return this.loader.hashCode() * 31 + this.resource.hashCode();
        }

    }


}
