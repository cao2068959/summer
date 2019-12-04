package com.chy.summer.link.common.multipart;

/**
 * MultipartForm的表单数据的分片接口
 */
public interface FormDataPart {

    /**
     * @return 名称
     */
    String name();

    /**
     * @return {@code true} 这部分是否是属性
     */
    boolean isAttribute();

    /**
     * @return {@code true} 这部分是否是上传的文件
     */
    boolean isFileUpload();

    /**
     * @return 如果这部分是表单属性，则为属性值，否则为null
     */
    String value();

    /**
     * @return 如果这部分是上传的文件，则为文件名，否则为null
     */
    String filename();

    /**
     * @return 如果这部分是上传的路径，则为文件路径，否则为null
     */
    String pathname();

    /**
     * @return 如果这部分是上传的路径，则为媒体类型，否则为null
     */
    String mediaType();

    /**
     * @return 如果这部分是上传的路径，则为返回文件类型，否则为null
     * {@code true} 文本文件 {@code false} 二进制文件
     */
    Boolean isText();

}