package com.chy.summer.framework.aop.support.annotation;

import com.chy.summer.framework.aop.ClassFilter;
import com.chy.summer.framework.core.annotation.AnnotationUtils;
import com.chy.summer.framework.util.Assert;

import java.lang.annotation.Annotation;

/**
 * 简单的ClassFilter，用于查找类中存在的特定Java注释
 * 通过检查目标类是否存在指定的注解，决定是否匹配
 */
public class AnnotationClassFilter implements ClassFilter {

	private final Class<? extends Annotation> annotationType;

	private final boolean checkInherited;


	/**
	 * 为给定的注释类型创建一个新的AnnotationClassFilter。
	 * @param annotationType 要查找的注释类型
	 */
	public AnnotationClassFilter(Class<? extends Annotation> annotationType) {
		this(annotationType, false);
	}

	/**
	 * 为给定的注释类型创建一个新的AnnotationClassFilter
	 * @param annotationType 要查找的注释类型
	 * @param checkInherited 是否还要检查超类和接口以及注释类型的元注释
	 */
	public AnnotationClassFilter(Class<? extends Annotation> annotationType, boolean checkInherited) {
		Assert.notNull(annotationType, "Annotation类型不可为空");
		this.annotationType = annotationType;
		this.checkInherited = checkInherited;
	}

	/**
	 * 切入点应该应用于给定的接口还是类
	 */
	@Override
	public boolean matches(Class<?> clazz) {
		return (this.checkInherited ?
				(AnnotationUtils.findAnnotation(clazz, this.annotationType) != null) :
				clazz.isAnnotationPresent(this.annotationType));
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AnnotationClassFilter)) {
			return false;
		}
		AnnotationClassFilter otherCf = (AnnotationClassFilter) other;
		return (this.annotationType.equals(otherCf.annotationType) && this.checkInherited == otherCf.checkInherited);
	}

	@Override
	public int hashCode() {
		return this.annotationType.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + this.annotationType;
	}

}
