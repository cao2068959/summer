package com.chy.summer.link.common.multipart.impl;

import com.chy.summer.link.common.multipart.FormDataPart;
import com.chy.summer.link.common.multipart.MultipartForm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 分片的form的实现
 */
public class MultipartFormImpl implements MultipartForm {
    /**
     * 切片的集合
     */
    private final List<FormDataPart> parts = new ArrayList<>();

    /**
     * 添加一个属性作为表单数据的一部分
     *
     * @param name  属性名
     * @param value 属性值
     * @return 返回当前对象，用于链式调用
     */
    @Override
    public MultipartForm attribute(String name, String value) {
        parts.add(new FormDataPartImpl(name, value));
        return this;
    }

    /**
     * 添加文本文件上传
     *
     * @param name      参数名称
     * @param filename  文件名
     * @param pathname  文件路径
     * @param mediaType 文件的MIME类型
     * @return 返回当前对象，用于链式调用
     */
    @Override
    public MultipartForm textFileUpload(String name, String filename, String pathname, String mediaType) {
        parts.add(new FormDataPartImpl(name, filename, pathname, mediaType, true));
        return this;
    }

    /**
     * 添加二进制文件上传
     *
     * @param name      参数名称
     * @param filename  文件名
     * @param pathname  文件路径
     * @param mediaType 文件的MIME类型
     * @return 返回当前对象，用于链式调用
     */
    @Override
    public MultipartForm binaryFileUpload(String name, String filename, String pathname, String mediaType) {
        parts.add(new FormDataPartImpl(name, filename, pathname, mediaType, false));
        return this;
    }

    /**
     * 迭代
     */
    @Override
    public Iterator<FormDataPart> iterator() {
        return parts.iterator();
    }
}