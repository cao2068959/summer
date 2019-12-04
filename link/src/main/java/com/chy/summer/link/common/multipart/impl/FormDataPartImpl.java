package com.chy.summer.link.common.multipart.impl;

import com.chy.summer.link.common.multipart.FormDataPart;

/**
 * MultipartForm的表单数据的分片实现
 */
public class FormDataPartImpl implements FormDataPart {
    /**
     * 名称
     */
    private final String name;
    /**
     * 如果这部分是表单属性，则为属性值
     */
    private final String value;
    /**
     * 如果这部分是上传的文件，则为文件名
     */
    private final String filename;
    /**
     * 如果这部分是上传的路径，则为媒体类型
     */
    private final String mediaType;
    /**
     * 如果这部分是上传的路径，则为文件路径
     */
    private final String pathname;
    /**
     * 如果这部分是上传的路径，则为返回文件类型
     * {@code true} 文本文件 {@code false} 二进制文件
     */
    private final Boolean text;

    public FormDataPartImpl(String name, String value) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (value == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.value = value;
        this.filename = null;
        this.pathname = null;
        this.mediaType = null;
        this.text = null;
    }

    public FormDataPartImpl(String name, String filename, String pathname, String mediaType, boolean text) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (filename == null) {
            throw new NullPointerException();
        }
        if (pathname == null) {
            throw new NullPointerException();
        }
        if (mediaType == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.value = null;
        this.filename = filename;
        this.pathname = pathname;
        this.mediaType = mediaType;
        this.text = text;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isAttribute() {
        return value != null;
    }

    @Override
    public boolean isFileUpload() {
        return value == null;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String filename() {
        return filename;
    }

    @Override
    public String pathname() {
        return pathname;
    }

    @Override
    public String mediaType() {
        return mediaType;
    }

    @Override
    public Boolean isText() {
        return text;
    }
}
