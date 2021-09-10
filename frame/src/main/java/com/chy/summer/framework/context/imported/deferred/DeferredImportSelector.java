package com.chy.summer.framework.context.imported.deferred;

import com.chy.summer.framework.context.imported.ImportSelector;


public interface DeferredImportSelector extends ImportSelector {

    default Class<? extends Group> getImportGroup() {
        return null;
    }


}
