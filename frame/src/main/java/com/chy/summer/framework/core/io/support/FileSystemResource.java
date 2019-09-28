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

/**
 * 文件系统资源对象
 * 用于解析文件资源
 */
public class FileSystemResource implements Resource {
    /**
     * 文件对象缓存
     */
    private final File file;

    /**
     * 文件地址
     */
    private final String path;

    /**
     * 初始化文件资源
     * @param file 文件对象
     */
    public FileSystemResource(File file) {
        this.file = file;
        this.path = StringUtils.cleanPath(file.getPath());
    }

    /**
     * 初始化文件资源
     * @param path 文件地址
     */
    public FileSystemResource(String path) {
        this.file = new File(path);
        this.path = StringUtils.cleanPath(path);
    }

    /**
     * 获取文件地址
     */
    public final String getPath() {
        return this.path;
    }

    /**
     * 判断文件是否存在
     */
    @Override
    public boolean exists() {
        return this.file.exists();
    }

    /**
     * 判断是否是个可读文件
     */
    @Override
    public boolean isReadable() {
        return (this.file.canRead() && !this.file.isDirectory());
    }


    @Override
    public boolean isOpen() {
        return false;
    }

    /**
     * 获取文件的输入流
     * @throws IOException
     */
    @Override
    public InputStream getInputStream() throws IOException {
        try {
            //File转换成Path 并创建输入流
            return Files.newInputStream(this.file.toPath());
        }
        catch (NoSuchFileException ex) {
            throw new FileNotFoundException(ex.getMessage());
        }
    }

    /**
     * 判断是否是个可写文件
     */
    public boolean isWritable() {
        return (this.file.canWrite() && !this.file.isDirectory());
    }

    /**
     * 获取文件的输出流
     * @throws IOException
     */
    public OutputStream getOutputStream() throws IOException {
        //File转换成Path 并创建输出流
        return Files.newOutputStream(this.file.toPath());
    }

    /**
     * 获取资源定位地址
     * @throws IOException
     */
    @Override
    public URL getURL() throws IOException {
        return this.file.toURI().toURL();
    }

    /**
     * 获取资源标识
     * @throws IOException
     */
    @Override
    public URI getURI() throws IOException {
        return this.file.toURI();
    }

    /**
     * 是否是文件
     */
    @Override
    public boolean isFile() {
        return true;
    }

    /**
     * 获取文件对象
     */
    @Override
    public File getFile() {
        return this.file;
    }

    /**
     * 获取文件字节读取通道
     * @throws IOException
     */
    @Override
    public ReadableByteChannel readableChannel() throws IOException {
        try {
            return FileChannel.open(this.file.toPath(), StandardOpenOption.READ);
        }
        catch (NoSuchFileException ex) {
            throw new FileNotFoundException(ex.getMessage());
        }
    }

    /**
     * 获取文件字节输出通道
     * @throws IOException
     */
    public WritableByteChannel writableChannel() throws IOException {
        return FileChannel.open(this.file.toPath(), StandardOpenOption.WRITE);
    }

    /**
     * 获取内容大小
     * @throws IOException
     */
    @Override
    public long contentLength() throws IOException {
        return this.file.length();
    }

    /**
     * 获取文件最后被修改的位置
     * @throws IOException
     */
    @Override
    public long lastModified() throws IOException {
        return 0;
    }

    /**
     * 根据相对路径创建文件资源对象(相对于这个资源文件的位置)
     * @param relativePath 相对地址路径
     * @return
     */
    @Override
    public Resource createRelative(String relativePath) {
        String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
        return new FileSystemResource(pathToUse);
    }

    /**
     * 获取文件名
     */
    @Override
    public String getFilename() {
        return this.file.getName();
    }

    /**
     * 获取文件描述
     * @return 返回file ["文件绝对路径"]
     */
    @Override
    public String getDescription() {
        return "file [" + this.file.getAbsolutePath() + "]";
    }

    /**
     * 判断文件是否是统一个文件
     * @param other
     * @return
     */
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
