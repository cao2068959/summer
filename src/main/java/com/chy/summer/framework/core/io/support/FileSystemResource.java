package com.chy.summer.framework.core.io.support;

import com.chy.summer.framework.util.StringUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;

public class FileSystemResource implements Resource {

    private final File file;

    private final String path;

    public FileSystemResource(File file) {
        this.file = file;
        this.path = StringUtils.cleanPath(file.getPath());
    }


    public FileSystemResource(String path) {
        this.file = new File(path);
        this.path = StringUtils.cleanPath(path);
    }



    public final String getPath() {
        return this.path;
    }

    @Override
    public boolean exists() {
        return this.file.exists();
    }

    @Override
    public boolean isReadable() {
        return (this.file.canRead() && !this.file.isDirectory());
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            return Files.newInputStream(this.file.toPath());
        }
        catch (NoSuchFileException ex) {
            throw new FileNotFoundException(ex.getMessage());
        }
    }


    public boolean isWritable() {
        return (this.file.canWrite() && !this.file.isDirectory());
    }


    public OutputStream getOutputStream() throws IOException {
        return Files.newOutputStream(this.file.toPath());
    }

    @Override
    public URL getURL() throws IOException {
        return this.file.toURI().toURL();
    }

    @Override
    public URI getURI() throws IOException {
        return this.file.toURI();
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public ReadableByteChannel readableChannel() throws IOException {
        try {
            return FileChannel.open(this.file.toPath(), StandardOpenOption.READ);
        }
        catch (NoSuchFileException ex) {
            throw new FileNotFoundException(ex.getMessage());
        }
    }


    public WritableByteChannel writableChannel() throws IOException {
        return FileChannel.open(this.file.toPath(), StandardOpenOption.WRITE);
    }

    @Override
    public long contentLength() throws IOException {
        return this.file.length();
    }

    @Override
    public long lastModified() throws IOException {
        return 0;
    }

    @Override
    public Resource createRelative(String relativePath) {
        String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
        return new FileSystemResource(pathToUse);
    }

    @Override
    public String getFilename() {
        return this.file.getName();
    }

    @Override
    public String getDescription() {
        return "file [" + this.file.getAbsolutePath() + "]";
    }


    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof FileSystemResource &&
                this.path.equals(((FileSystemResource) other).path)));
    }

    @Override
    public int hashCode() {
        return this.path.hashCode();
    }
}
