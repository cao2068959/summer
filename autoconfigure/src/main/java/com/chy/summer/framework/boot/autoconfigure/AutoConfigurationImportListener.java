package com.chy.summer.framework.boot.autoconfigure;


import java.util.EventListener;

public interface AutoConfigurationImportListener extends EventListener {

    void onAutoConfigurationImportEvent(AutoConfigurationImportEvent event);

}
