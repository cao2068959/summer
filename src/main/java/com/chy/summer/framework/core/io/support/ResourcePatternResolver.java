package com.chy.summer.framework.core.io.support;

import java.io.IOException;

public interface ResourcePatternResolver  {

    public  static String CLASSPATH_ALL_URL_PREFIX = "classpath*:";

    Resource[] getResources(String locationPattern) throws IOException;

}
