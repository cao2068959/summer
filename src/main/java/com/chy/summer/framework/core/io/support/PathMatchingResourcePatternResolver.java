package com.chy.summer.framework.core.io.support;

import com.chy.summer.framework.core.io.DefaultResourceLoader;
import com.chy.summer.framework.core.io.ResourceLoader;
import com.chy.summer.framework.core.type.classreading.DefaultMetadataReaderFactory;
import com.chy.summer.framework.core.type.classreading.MetadataReaderFactory;
import jdk.internal.org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class PathMatchingResourcePatternResolver implements ResourcePatternResolver{

    /**
     * 资源加载器
     */
    ResourceLoader resourceLoader;

    /**
     * 初始化资源加载器
     */
    public PathMatchingResourcePatternResolver() {
        this.resourceLoader = new DefaultResourceLoader();
    }

    /**
     * 获取资源加载器
     */
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    /**
     * 获取指定路径下的class对象
     *
     * @param locationPattern 需要检索的包路径
     * @return
     * @throws IOException
     */
    @Override
    public Resource[] getResources(String locationPattern) throws IOException {
        URL resourceURL = null;
        //如果是classpath*： 开头的把 classpath*: 给换成 classes文件夹的绝对路径
        if(locationPattern.startsWith(CLASSPATH_ALL_URL_PREFIX)){
            String location  = locationPattern.substring(CLASSPATH_ALL_URL_PREFIX.length());
            //获取绝对路径
            resourceURL = getResourceLoader().getClassLoader().getResource(location);
        }else{
            resourceURL = new URL(locationPattern);
        }
        //通过url 把 所有的文件扫描出来，放set里
        Set<Resource> resources = doFindPathMatchingFileResources(resourceURL);
        return resources.toArray(new Resource[0]);

    }


    /**
     * 真正扫描文件的方法
     * @param url 硬盘路径可能是绝对路径 也可能是相对路径
     * @return 指定路径下的所有class类型的文件
     */
    private Set<Resource> doFindPathMatchingFileResources(URL url){
        //硬盘索引指定路径的文件或文件夹
        File rootFile = new File(url.getFile());
        Set<Resource> result = new HashSet<>();
        //查询后缀名为class的文件
        fileHandle(result,rootFile,"class");
        return result;
    }

    /**
     * 递归扫描所有的class文件
     * @param result 存放文件的容器对象
     * @param file 硬盘索引到的文件或者文件夹
     * @param type 文件类型（文件后缀名)
     */
    private void fileHandle(Set<Resource> result,File file , String type){

        //如果是文件夹就递归扫描
        if(file.isDirectory()){
            Arrays.stream(Optional.ofNullable(file.list()).orElse(new String[0])).forEach(path->{
                fileHandle(result,new File(file.getPath()+"/"+path),type);
            });
            return;
        }
        //获取文件名
        String fileName = file.getName();
        //获取后缀名
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        //判断是否是指定的后缀文件
        if(!type.equals(suffix)){
            return;
        }
        //将文件解析成资源对象
        Resource fileSystemResource = new FileSystemResource(file);
        //放入容器
        result.add(fileSystemResource);

    }








}

