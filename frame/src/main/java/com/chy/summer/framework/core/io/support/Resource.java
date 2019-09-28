package com.chy.summer.framework.core.io.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public interface Resource {

    boolean exists();


    default boolean isReadable() {
        return true;
    }


    default boolean isOpen() {
        return false;
    }


    default boolean isFile() {
        return false;
    }


    URL getURL() throws IOException;


    URI getURI() throws IOException;


    File getFile() throws IOException;


    default ReadableByteChannel readableChannel() throws IOException {
        return Channels.newChannel(getInputStream());
    }


    long contentLength() throws IOException;


    long lastModified() throws IOException;


    Resource createRelative(String relativePath) throws IOException;


    String getFilename();


    String getDescription();

    InputStream getInputStream() throws IOException;

}
