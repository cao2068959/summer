package com.chy.summer.framework.context.imported.deferred;

import com.chy.summer.framework.context.annotation.ConfigurationClass;
import com.chy.summer.framework.context.annotation.ConfigurationClassParser;
import com.chy.summer.framework.core.ordered.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于延迟导入一些 bean对象的处理器
 *
 */
public class DeferredImportSelectorHandler {

    private final ConfigurationClassParser configurationClassParser;

    private List<DeferredImportSelectorHolder> deferredImportSelectors = new ArrayList<>();



    public DeferredImportSelectorHandler(ConfigurationClassParser configurationClassParser) {
        this.configurationClassParser = configurationClassParser;
    }

    /**
     *  当 deferredImportSelectors = null 的时候说明正在处理，这时候就直接开始去处理新进来的 DeferredImportSelectorHolder 对象
     *
     *  正常时刻，应该都是在不停的add holder进入队列等待处理
     *
     * @param configClass
     * @param importSelector
     */
    public void handle(ConfigurationClass configClass, DeferredImportSelector importSelector) {
        DeferredImportSelectorHolder holder = new DeferredImportSelectorHolder(configClass, importSelector);
        if (this.deferredImportSelectors == null) {
            DeferredImportSelectorGroupingHandler handler = new DeferredImportSelectorGroupingHandler(configurationClassParser);
            handler.register(holder);
            handler.processGroupImports();
        }
        else {
            this.deferredImportSelectors.add(holder);
        }
    }

    /**
     * 开始去处理所有的 holder
     *
     */
    public void process() {
        List<DeferredImportSelectorHolder> deferredImports = this.deferredImportSelectors;
        this.deferredImportSelectors = null;
        try {
            if (deferredImports != null) {
                DeferredImportSelectorGroupingHandler handler = new DeferredImportSelectorGroupingHandler(configurationClassParser);
                deferredImports.sort((o1, o2) ->
                        AnnotationAwareOrderComparator.INSTANCE.compare(o1.getImportSelector(), o2.getImportSelector()));
                deferredImports.forEach(handler::register);
                handler.processGroupImports();
            }
        }
        finally {
            this.deferredImportSelectors = new ArrayList<>();
        }
    }


}
