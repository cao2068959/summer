package com.chy.summer.framework.core.convert.converter;

import javax.annotation.Nullable;

@FunctionalInterface
public interface Converter<S, T> {

	/**
	 * 将类型S的源对象转换为目标类型T
	 * @param source 要转换的源对象，该对象必须是S的实例（决不能为null）
	 */
	@Nullable
	T convert(S source);

}