package com.chy.summer.framework.core.convert.converter;


import com.chy.summer.framework.core.convert.support.ConversionService;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.comparator.Comparators;
import com.sun.istack.internal.Nullable;

import java.util.Comparator;
import java.util.Map;

/**
 * 类型装换比较器，在比较值之前先将其转换成相同类型
 * 使用指定的Converter将每个值传递给Comparator之前的对象进行转换，让后再进行比较
 */
public class ConvertingComparator<S, T> implements Comparator<S> {

	/**
	 * 比较器
	 */
	private final Comparator<T> comparator;

	/**
	 * 类型转换器
	 */
	private final Converter<S, T> converter;


	/**
	 * 创建一个ConvertingComparator实例
	 */
	public ConvertingComparator(Converter<S, T> converter) {
		this(Comparators.comparable(), converter);
	}

	/**
	 * 创建一个ConvertingComparator实例
	 */
	public ConvertingComparator(Comparator<T> comparator, Converter<S, T> converter) {
		Assert.notNull(comparator, "Comparator不可为空");
		Assert.notNull(converter, "Converter不可为空");
		this.comparator = comparator;
		this.converter = converter;
	}

	/**
	 * 创建一个ConvertingComparator实例
	 * @param comparator 底层比较器
	 * @param conversionService 转换服务
	 * @param targetType 目标类型
	 */
	public ConvertingComparator(
			Comparator<T> comparator, ConversionService conversionService, Class<? extends T> targetType) {

		this(comparator, new ConversionServiceConverter<>(conversionService, targetType));
	}


	@Override
	public int compare(S o1, S o2) {
		T c1 = this.converter.convert(o1);
		T c2 = this.converter.convert(o2);
		return this.comparator.compare(c1, c2);
	}

	/**
	 * 创建一个新的ConvertingComparator，用于比较 Map.Entry,根据其Map.Entry＃getKey（）比较
	 * @param comparator key的基础比较器
	 */
	public static <K, V> ConvertingComparator<Map.Entry<K, V>, K> mapEntryKeys(Comparator<K> comparator) {
		return new ConvertingComparator<>(comparator, source -> source.getKey());
	}

	/**
	 * 创建一个新的ConvertingComparator，用于比较 Map.Entry,根据其Map.Entry＃getValue（）比较
	 * @param comparator value的基础比较器
	 */
	public static <K, V> ConvertingComparator<Map.Entry<K, V>, V> mapEntryValues(Comparator<V> comparator) {
		return new ConvertingComparator<>(comparator, source -> source.getValue());
	}


	/**
	 * 将ConversionService和targetType封装为转换器
	 */
	private static class ConversionServiceConverter<S, T> implements Converter<S, T> {

		private final ConversionService conversionService;

		private final Class<? extends T> targetType;

		public ConversionServiceConverter(ConversionService conversionService,
										  Class<? extends T> targetType) {
			Assert.notNull(conversionService, "ConversionService不可为空");
			Assert.notNull(targetType, "TargetType不可为空");
			this.conversionService = conversionService;
			this.targetType = targetType;
		}

		@Override
		@Nullable
		public T convert(S source) {
			return this.conversionService.convert(source, this.targetType);
		}
	}

}