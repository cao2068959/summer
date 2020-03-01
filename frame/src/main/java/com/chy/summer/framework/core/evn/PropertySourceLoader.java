package com.chy.summer.framework.core.evn;

import com.chy.summer.framework.core.evn.propertysource.PropertySource;
import com.chy.summer.framework.core.io.support.Resource;

import java.io.IOException;
import java.util.List;

/**
 * 配置文件加载器的接口
 */
public interface PropertySourceLoader {


    /**
     * 获取文件的后缀
     * @return
     */
    String[] getFileExtensions();

    /**
     * 加载配置文件
     * @param name
     * @param resource
     * @return
     * @throws IOException
     */
    List<PropertySource<?>> load(String name, Resource resource) throws IOException;

}
