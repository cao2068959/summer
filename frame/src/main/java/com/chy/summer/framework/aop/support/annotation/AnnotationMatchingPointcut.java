package com.chy.summer.framework.aop.support.annotation;

import com.chy.summer.framework.aop.ClassFilter;
import com.chy.summer.framework.aop.MethodMatcher;
import com.chy.summer.framework.aop.Pointcut;
import com.chy.summer.framework.util.Assert;
import com.sun.istack.internal.Nullable;

import java.lang.annotation.Annotation;

/**
 * 简单的切入点，它查找存在于forClassAnnotation类或forMethodAnnotation方法上的特定Java注解。
 */
public class AnnotationMatchingPointcut implements Pointcut {

	private final ClassFilter classFilter;

	private final MethodMatcher methodMatcher;


	/**
	 * 为给定的注释类型创建一个新的AnnotationMatchingPointcut
	 * @param classAnnotationType 在类的级别上查找的注释类型
	 */
	public AnnotationMatchingPointcut(Class<? extends Annotation> classAnnotationType) {
		this(classAnnotationType, false);
	}

	/**
	 * 为给定的注释类型创建一个新的AnnotationMatchingPointcut。
	 * @param classAnnotationType 在类的级别上查找的注释类型
	 * @param checkInherited 是否还要检查超类和接口以及注释类型的元注释
	 */
	public AnnotationMatchingPointcut(Class<? extends Annotation> classAnnotationType, boolean checkInherited) {
		this.classFilter = new AnnotationClassFilter(classAnnotationType, checkInherited);
		this.methodMatcher = MethodMatcher.TRUE;
	}

	/**
	 * 为给定的注释类型创建一个新的AnnotationMatchingPointcut
	 * @param classAnnotationType 在类的级别上查找的注释类型，可以为空
	 * @param methodAnnotationType 在方法级别寻找的注释类型，可以为空
	 */
	public AnnotationMatchingPointcut(@Nullable Class<? extends Annotation> classAnnotationType,
			@Nullable Class<? extends Annotation> methodAnnotationType) {

		this(classAnnotationType, methodAnnotationType, false);
	}

	/**
	 * 为给定的注释类型创建一个新的AnnotationMatchingPointcut
	 * @param classAnnotationType 在类的级别上查找的注释类型，可以为空
	 * @param methodAnnotationType 在方法级别寻找的注释类型，可以为空
	 * @param checkInherited 是否还要检查超类和接口以及注释类型的元注释
	 */
	public AnnotationMatchingPointcut(@Nullable Class<? extends Annotation> classAnnotationType,
			@Nullable Class<? extends Annotation> methodAnnotationType, boolean checkInherited) {

		Assert.isTrue((classAnnotationType != null || methodAnnotationType != null),
				"需要指定类注释类型或方法注释类型(或两者都需要)");

		if (classAnnotationType != null) {
			this.classFilter = new AnnotationClassFilter(classAnnotationType, checkInherited);
		}
		else {
			this.classFilter = ClassFilter.TRUE;
		}

		if (methodAnnotationType != null) {
			//TODO GYX 写到这里
			this.methodMatcher = MethodMatcher.TRUE;
//			this.methodMatcher = new AnnotationMethodMatcher(methodAnnotationType, checkInherited);
		}
		else {
			this.methodMatcher = MethodMatcher.TRUE;
		}
	}


	@Override
	public ClassFilter getClassFilter() {
		return this.classFilter;
	}

	@Override
	public MethodMatcher getMethodMatcher() {
		return this.methodMatcher;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AnnotationMatchingPointcut)) {
			return false;
		}
		AnnotationMatchingPointcut otherPointcut = (AnnotationMatchingPointcut) other;
		return (this.classFilter.equals(otherPointcut.classFilter) &&
				this.methodMatcher.equals(otherPointcut.methodMatcher));
	}

	@Override
	public int hashCode() {
		return this.classFilter.hashCode() * 37 + this.methodMatcher.hashCode();
	}

	@Override
	public String toString() {
		return "AnnotationMatchingPointcut: " + this.classFilter + ", " +this.methodMatcher;
	}


	/**
	 * AnnotationMatchingPointcut的工厂方法，该方法在类级别与指定的注释匹配
	 * @param annotationType 在类级别上查找的注解类型
	 */
	public static AnnotationMatchingPointcut forClassAnnotation(Class<? extends Annotation> annotationType) {
		Assert.notNull(annotationType, "Annotation类型不可为空");
		return new AnnotationMatchingPointcut(annotationType);
	}

	/**
	 * AnnotationMatchingPointcut的工厂方法，在方法级别与指定的注释匹配。
	 * @param annotationType 在类级别上查找的注解类型
	 */
	public static AnnotationMatchingPointcut forMethodAnnotation(Class<? extends Annotation> annotationType) {
		Assert.notNull(annotationType, "Annotation类型不可为空");
		return new AnnotationMatchingPointcut(null, annotationType);
	}

}