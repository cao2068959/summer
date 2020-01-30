package com.chy.summer.framework.core.evn;

import com.chy.summer.framework.core.evn.resolver.PropertyResolver;

public interface Environment extends PropertyResolver {

    String[] getActiveProfiles();

    String[] getDefaultProfiles();

}
