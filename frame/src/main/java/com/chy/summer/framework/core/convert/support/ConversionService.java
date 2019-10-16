package com.chy.summer.framework.core.convert.support;

import com.sun.istack.internal.Nullable;

public interface ConversionService {
	@Nullable
	<T> T convert(@Nullable Object source, Class<T> targetType);
	//TODO GYX 这里留坑 ，需要重写类型装换
}
