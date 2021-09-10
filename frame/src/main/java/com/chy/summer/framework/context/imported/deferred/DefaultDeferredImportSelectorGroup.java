package com.chy.summer.framework.context.imported.deferred;


import com.chy.summer.framework.core.type.AnnotationMetadata;

public class DefaultDeferredImportSelectorGroup implements Group {



    @Override
    public void process(AnnotationMetadata metadata, DeferredImportSelector selector) {

    }

    @Override
    public Iterable<Entry> selectImports() {
        return null;
    }
}
