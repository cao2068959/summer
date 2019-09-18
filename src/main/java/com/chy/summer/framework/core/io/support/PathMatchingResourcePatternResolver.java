package com.chy.summer.framework.core.io.support;

import com.chy.summer.framework.core.io.DefaultResourceLoader;
import com.chy.summer.framework.core.io.ResourceLoader;
import com.chy.summer.framework.core.type.classreading.DefaultMetadataReaderFactory;
import com.chy.summer.framework.core.type.classreading.MetadataReaderFactory;
import jdk.internal.org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
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
        return resources.toArray(new Resource[0]);

    }



    /**
     *  真正扫描文件的方法
     */
    private Set<Resource> doFindPathMatchingFileResources(URL url){
        File rootFile = new File(url.getFile());
        Set<Resource> result = new HashSet<>();
        fileHandle(result,rootFile,"class");
        return result;
    }

    /**
     * 递归扫描所有的class文件
     */
    private void fileHandle(Set<Resource> result,File file , String type){

        //如果是文件夹就递归扫描
        if(file.isDirectory()){
            Arrays.stream(file.list()).forEach(path->{
                fileHandle(result,new File(file.getPath()+"/"+path),type);
            });
            return;
        }

        String fileName = file.getName();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        //判断是否是指定的后缀文件
        if(!type.equals(suffix)){
            return;
        }
        Resource fileSystemResource = new FileSystemResource(file);
        result.add(fileSystemResource);

    }








}

