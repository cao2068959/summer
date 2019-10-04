package com.chy.summer.framework.core.io.support;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public abstract class AbstractFileResolvingResource implements Resource {

    @Override
    public boolean exists() {
        try {
            return getFile().exists();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public boolean isReadable() {
        try {
            File file = getFile();
            return (file.canRead() && !file.isDirectory());
        }
        catch (IOException ex) {
            return false;
        }
    }

    @Override
    public boolean isFile() {
        try {
            URL url = getURL();
            return ResourceUtils.URL_PROTOCOL_FILE.equals(url.getProtocol());
        }
        catch (IOException ex) {
            return false;
        }
    }

    @Override
    public File getFile() throws IOException {
        URL url = getURL();
        return ResourceUtils.getFile(url, getDescription());
    }





}
