package com.chy.summer.framework.core.convert;

import org.apache.derby.catalog.TypeDescriptor;

public interface ConversionService {

    /**
     *  判断 2个类型是否可以相互转换
     * @param sourceType
     * @param targetType
     * @return
     */
    boolean canConvert(Class<?> sourceType, Class<?> targetType);

    /**
     *  判断 2个类型是否可以相互转换
     * @param sourceType
     * @param targetType
     * @return
     */
    boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType);

    /**
     * 转换类型
     * @param source
     * @param targetType
     * @param <T>
     * @return
     */
    <T> T convert(Object source, Class<T> targetType);

    /**
     * 转换类型
     * @param source
     * @param sourceType
     * @param targetType
     * @return
     */
    Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType);


}
