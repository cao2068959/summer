package com.chy.summer.framework.core.io.support;

import com.chy.summer.framework.core.io.DefaultResourceLoader;
import com.chy.summer.framework.core.io.ResourceLoader;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

public class PathMatchingResourcePatternResolver implements ResourcePatternResolver{


    ResourceLoader resourceLoader;

    public PathMatchingResourcePatternResolver() {
        this.resourceLoader = new DefaultResourceLoader();
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }


    @Override
    public Resource[] getResources(String locationPattern) throws IOException {
        URL resourceURL = null;
        //如果是classpath*： 开头的把 classpath*: 给换成 classes文件夹的绝对路径
        if(locationPattern.startsWith(CLASSPATH_ALL_URL_PREFIX)){
            String location  = locationPattern.substring(CLASSPATH_ALL_URL_PREFIX.length());
            resourceURL = getResourceLoader().getClassLoader().getResource(location);
        }else{
            resourceURL = new URL(locationPattern);
        }

        //通过url 把 所有的文件扫描出来，放set里
        Set<Resource> resources = doFindPathMatchingFileResources(resourceURL);
        return null;
    }

    /**
     *  真正扫描文件的方法
     */
    private Set<Resource> doFindPathMatchingFileResources(URL url){
       return null;
    }






}

