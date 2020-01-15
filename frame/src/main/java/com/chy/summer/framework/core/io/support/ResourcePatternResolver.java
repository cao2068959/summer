package com.chy.summer.framework.core.io.support;

import com.chy.summer.framework.core.io.ResourceLoader;

import java.io.IOException;

public interface ResourcePatternResolver extends ResourceLoader {

    /**
     * 在根目录所有文件的前缀，带有这个前缀的包路径表示会在所有的jar包的根目录下开始搜索指定文件
     */
    public static String CLASSPATH_ALL_URL_PREFIX = "classpath*:";

    /**
     * 获取指定路径下的所有资源对象
     *
     * @param locationPattern 需要检索的包路径
     * @return 返回指定路径下的资源对象,同一个重复的资源，只会返回一个资源对象
     * @throws IOException
     */
    Resource[] getResources(String locationPattern) throws IOException;

}
