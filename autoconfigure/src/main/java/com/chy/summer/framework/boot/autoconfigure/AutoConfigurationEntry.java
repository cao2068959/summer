package com.chy.summer.framework.boot.autoconfigure;


import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Getter
public class AutoConfigurationEntry {

    private final List<String> configurations;
    private final Set<String> exclusions;

    public AutoConfigurationEntry() {
        this.configurations = Collections.emptyList();
        this.exclusions = Collections.emptySet();
    }


    public AutoConfigurationEntry(List<String> configurations, Set<String> exclusions) {
        this.configurations = configurations;
        this.exclusions = exclusions;
    }
}
