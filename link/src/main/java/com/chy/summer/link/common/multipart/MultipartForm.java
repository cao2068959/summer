package com.chy.summer.link.common.multipart;

import com.chy.summer.link.common.multipart.impl.MultipartFormImpl;

/**
 * 分片的form的接口
 */
public interface MultipartForm extends Iterable<FormDataPart> {

    /**
     * @return 创建一个实例
     */
    static MultipartForm create() {
        return new MultipartFormImpl();
    }

    /**
     * 添加一个属性作为表单数据的一部分
     *
     * @param name  属性名
     * @param value 属性值
     * @return 返回当前对象，用于链式调用
     */
    MultipartForm attribute(String name, String value);

    /**
     * 添加文本文件上传
     *
     * @param name      参数名称
     * @param filename  文件名
     * @param pathname  文件路径
     * @param mediaType 文件的MIME类型
     * @return 返回当前对象，用于链式调用
     */
    MultipartForm textFileUpload(String name, String filename, String pathname, String mediaType);

    /**
     * 添加二进制文件上传
     *
     * @param name      参数名称
     * @param filename  文件名
     * @param pathname  文件路径
     * @param mediaType 文件的MIME类型
     * @return 返回当前对象，用于链式调用
     */
    MultipartForm binaryFileUpload(String name, String filename, String pathname, String mediaType);

}