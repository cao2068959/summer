package com.chy.summer.framework.core.io;

import com.chy.summer.framework.core.io.support.AbstractFileResolvingResource;
import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.StringUtils;
import javax.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.channels.ReadableByteChannel;

public class UrlResource extends AbstractFileResolvingResource {


    private final URI uri;


    private final URL url;


    private final URL cleanedUrl;



    public UrlResource(URI uri) throws MalformedURLException {
        Assert.notNull(uri, "URI must not be null");
        this.uri = uri;
        this.url = uri.toURL();
        this.cleanedUrl = getCleanedUrl(this.url, uri.toString());
    }


    public UrlResource(URL url) {
        Assert.notNull(url, "URL must not be null");
        this.url = url;
        this.cleanedUrl = getCleanedUrl(this.url, url.toString());
        this.uri = null;
    }


    public UrlResource(String path) throws MalformedURLException {
        Assert.notNull(path, "Path must not be null");
        this.uri = null;
        this.url = new URL(path);
        this.cleanedUrl = getCleanedUrl(this.url, path);
    }


    public UrlResource(String protocol, String location) throws MalformedURLException  {
        this(protocol, location, null);
    }


    public UrlResource(String protocol, String location, @Nullable String fragment) throws MalformedURLException  {
        try {
            this.uri = new URI(protocol, location, fragment);
            this.url = this.uri.toURL();
            this.cleanedUrl = getCleanedUrl(this.url, this.uri.toString());
        }
        catch (URISyntaxException ex) {
            MalformedURLException exToThrow = new MalformedURLException(ex.getMessage());
            exToThrow.initCause(ex);
            throw exToThrow;
        }
    }



    private URL getCleanedUrl(URL originalUrl, String originalPath) {
        try {
            return new URL(StringUtils.cleanPath(originalPath));
        }
        catch (MalformedURLException ex) {
            return originalUrl;
        }
    }


    @Override
    public InputStream getInputStream() throws IOException {
        URLConnection con = this.url.openConnection();
        try {
            return con.getInputStream();
        }
        catch (IOException ex) {
            if (con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).disconnect();
            }
            throw ex;
        }
    }


    @Override
    public URL getURL() {
        return this.url;
    }


    @Override
    public URI getURI() throws IOException {
        return this.uri;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isFile() {
        return super.isFile();
    }


    @Override
    public File getFile() throws IOException {
        return super.getFile();
    }

    @Override
    public ReadableByteChannel readableChannel() throws IOException {
        return null;
    }

    @Override
    public long contentLength() throws IOException {
        return 0;
    }

    @Override
    public long lastModified() throws IOException {
        return 0;
    }


    @Override
    public Resource createRelative(String relativePath) throws MalformedURLException {
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return new UrlResource(new URL(this.url, relativePath));
    }


    @Override
    public String getFilename() {
        return StringUtils.getFilename(this.cleanedUrl.getPath());
    }


    @Override
    public String getDescription() {
        return "URL [" + this.url + "]";
    }



    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof UrlResource &&
                this.cleanedUrl.equals(((UrlResource) other).cleanedUrl)));
    }


    @Override
    public int hashCode() {
        return this.cleanedUrl.hashCode();
    }
}
