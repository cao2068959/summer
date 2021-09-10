package com.chy.summer.framework.context.imported.deferred;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DeferredImportSelectorGrouping {

    private final Group group;

    private final List<DeferredImportSelectorHolder> deferredImports = new ArrayList<>();

    public DeferredImportSelectorGrouping(Group group) {
        this.group = group;
    }

    public void add(DeferredImportSelectorHolder deferredImport) {
        this.deferredImports.add(deferredImport);
    }



    public Iterable<Group.Entry> getImports() {
        for (DeferredImportSelectorHolder deferredImport : this.deferredImports) {
            this.group.process(deferredImport.getConfigurationClass().getMetadata(),
                    deferredImport.getImportSelector());
        }
        return this.group.selectImports();
    }

    public Predicate<String> getCandidateFilter() {
        Predicate<String> mergedFilter = className ->
                (className.startsWith("java.lang.annotation.") ||
                        className.startsWith("com.chy.summer.framework.annotation.stereotype."));
        for (DeferredImportSelectorHolder deferredImport : this.deferredImports) {
            Predicate<String> selectorFilter = deferredImport.getImportSelector().getExclusionFilter();
            if (selectorFilter != null) {
                mergedFilter = mergedFilter.or(selectorFilter);
            }
        }
        return mergedFilter;
    }
}
