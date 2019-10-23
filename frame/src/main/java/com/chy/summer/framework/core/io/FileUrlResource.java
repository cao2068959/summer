package com.chy.summer.framework.core.io;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class FileUrlResource extends UrlResource {

    private volatile File file;

    public FileUrlResource(URL url) {
        super(url);
    }
}
