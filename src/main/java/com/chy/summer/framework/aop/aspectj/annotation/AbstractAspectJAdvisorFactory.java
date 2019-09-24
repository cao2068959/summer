package com.chy.summer.framework.aop.aspectj.annotation;

import com.chy.summer.framework.aop.framework.AopConfigException;
import com.chy.summer.framework.aop.framework.NotAnAtAspectException;
import com.chy.summer.framework.core.annotation.AnnotationUtils;
import com.chy.summer.framework.util.StringUtils;
import com.sun.istack.internal.Nullable;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PerClauseKind;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public abstract class AbstractAspectJAdvisorFactory{
	//TODO 没有写完
//public abstract class AbstractAspectJAdvisorFactory implements AspectJAdvisorFactory {

//	private static final String AJC_MAGIC = "ajc$";
//
//	protected final ParameterNameDiscoverer parameterNameDiscoverer = new AspectJAnnotationParameterNameDiscoverer();
//
//
//	/**
//	 * We consider something to be an AspectJ aspect suitable for use by the Spring AOP system
//	 * if it has the @Aspect annotation, and was not compiled by ajc. The reason for this latter test
//	 * is that aspects written in the code-style (AspectJ language) also have the annotation present
//	 * when compiled by ajc with the -1.5 flag, yet they cannot be consumed by Spring AOP.
//	 */
//	@Override
//	public boolean isAspect(Class<?> clazz) {
//		return (hasAspectAnnotation(clazz) && !compiledByAjc(clazz));
//	}
//
//	private boolean hasAspectAnnotation(Class<?> clazz) {
//		return (AnnotationUtils.findAnnotation(clazz, Aspect.class) != null);
//	}
//
//	/**
//	 * We need to detect this as "code-style" AspectJ aspects should not be
//	 * interpreted by Spring AOP.
//	 */
//	private boolean compiledByAjc(Class<?> clazz) {
//		// The AJTypeSystem goes to great lengths to provide a uniform appearance between code-style and
//		// annotation-style aspects. Therefore there is no 'clean' way to tell them apart. Here we rely on
//		// an implementation detail of the AspectJ compiler.
//		for (Field field : clazz.getDeclaredFields()) {
//			if (field.getName().startsWith(AJC_MAGIC)) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	@Override
//	public void validate(Class<?> aspectClass) throws AopConfigException {
//		// If the parent has the annotation and isn't abstract it's an error
//		if (aspectClass.getSuperclass().getAnnotation(Aspect.class) != null &&
//				!Modifier.isAbstract(aspectClass.getSuperclass().getModifiers())) {
//			throw new AopConfigException("[" + aspectClass.getName() + "] cannot extend concrete aspect [" +
//					aspectClass.getSuperclass().getName() + "]");
//		}
//
//		AjType<?> ajType = AjTypeSystem.getAjType(aspectClass);
//		if (!ajType.isAspect()) {
//			throw new NotAnAtAspectException(aspectClass);
//		}
//		if (ajType.getPerClause().getKind() == PerClauseKind.PERCFLOW) {
//			throw new AopConfigException(aspectClass.getName() + " uses percflow instantiation model: " +
//					"This is not supported in Spring AOP.");
//		}
//		if (ajType.getPerClause().getKind() == PerClauseKind.PERCFLOWBELOW) {
//			throw new AopConfigException(aspectClass.getName() + " uses percflowbelow instantiation model: " +
//					"This is not supported in Spring AOP.");
//		}
//	}
//
//	/**
//	 * Find and return the first AspectJ annotation on the given method
//	 * (there <i>should</i> only be one anyway...)
//	 */
//	@Nullable
//	protected static AspectJAnnotation<?> findAspectJAnnotationOnMethod(Method method) {
//		Class<?>[] classesToLookFor = new Class<?>[] {
//				Before.class, Around.class, After.class, AfterReturning.class, AfterThrowing.class, Pointcut.class};
//		for (Class<?> c : classesToLookFor) {
//			AspectJAnnotation<?> foundAnnotation = findAnnotation(method, (Class<Annotation>) c);
//			if (foundAnnotation != null) {
//				return foundAnnotation;
//			}
//		}
//		return null;
//	}
//
//	@Nullable
//	private static <A extends Annotation> AspectJAnnotation<A> findAnnotation(Method method, Class<A> toLookFor) {
//		A result = AnnotationUtils.findAnnotation(method, toLookFor);
//		if (result != null) {
//			return new AspectJAnnotation<>(result);
//		}
//		else {
//			return null;
//		}
//	}
//
//
//	protected enum AspectJAnnotationType {
//
//		AtPointcut,
//		AtBefore,
//		AtAfter,
//		AtAfterReturning,
//		AtAfterThrowing,
//		AtAround
//	}
//
//
//	/**
//	 * Class modelling an AspectJ annotation, exposing its type enumeration and
//	 * pointcut String.
//	 */
//	protected static class AspectJAnnotation<A extends Annotation> {
//
//		private static final String[] EXPRESSION_PROPERTIES = new String[] {"value", "pointcut"};
//
//		private static Map<Class<?>, AspectJAnnotationType> annotationTypes = new HashMap<>();
//
//		static {
//			annotationTypes.put(Pointcut.class,AspectJAnnotationType.AtPointcut);
//			annotationTypes.put(After.class,AspectJAnnotationType.AtAfter);
//			annotationTypes.put(AfterReturning.class,AspectJAnnotationType.AtAfterReturning);
//			annotationTypes.put(AfterThrowing.class,AspectJAnnotationType.AtAfterThrowing);
//			annotationTypes.put(Around.class,AspectJAnnotationType.AtAround);
//			annotationTypes.put(Before.class,AspectJAnnotationType.AtBefore);
//		}
//
//		private final A annotation;
//
//		private final AspectJAnnotationType annotationType;
//
//		private final String pointcutExpression;
//
//		private final String argumentNames;
//
//		public AspectJAnnotation(A annotation) {
//			this.annotation = annotation;
//			this.annotationType = determineAnnotationType(annotation);
//			// We know these methods exist with the same name on each object,
//			// but need to invoke them reflectively as there isn't a common interface.
//			try {
//				this.pointcutExpression = resolveExpression(annotation);
//				this.argumentNames = (String) annotation.getClass().getMethod("argNames").invoke(annotation);
//			}
//			catch (Exception ex) {
//				throw new IllegalArgumentException(annotation + " cannot be an AspectJ annotation", ex);
//			}
//		}
//
//		private AspectJAnnotationType determineAnnotationType(A annotation) {
//			for (Class<?> type : annotationTypes.keySet()) {
//				if (type.isInstance(annotation)) {
//					return annotationTypes.get(type);
//				}
//			}
//			throw new IllegalStateException("Unknown annotation type: " + annotation.toString());
//		}
//
//		private String resolveExpression(A annotation) throws Exception {
//			for (String methodName : EXPRESSION_PROPERTIES) {
//				Method method;
//				try {
//					method = annotation.getClass().getDeclaredMethod(methodName);
//				}
//				catch (NoSuchMethodException ex) {
//					method = null;
//				}
//				if (method != null) {
//					String candidate = (String) method.invoke(annotation);
//					if (StringUtils.hasText(candidate)) {
//						return candidate;
//					}
//				}
//			}
//			throw new IllegalStateException("Failed to resolve expression: " + annotation);
//		}
//
//		public AspectJAnnotationType getAnnotationType() {
//			return this.annotationType;
//		}
//
//		public A getAnnotation() {
//			return this.annotation;
//		}
//
//		public String getPointcutExpression() {
//			return this.pointcutExpression;
//		}
//
//		public String getArgumentNames() {
//			return this.argumentNames;
//		}
//
//		@Override
//		public String toString() {
//			return this.annotation.toString();
//		}
//	}
//
//
//	/**
//	 * ParameterNameDiscoverer implementation that analyzes the arg names
//	 * specified at the AspectJ annotation level.
//	 */
//	private static class AspectJAnnotationParameterNameDiscoverer implements ParameterNameDiscoverer {
//
//		@Override
//		@Nullable
//		public String[] getParameterNames(Method method) {
//			if (method.getParameterCount() == 0) {
//				return new String[0];
//			}
//			AspectJAnnotation<?> annotation = findAspectJAnnotationOnMethod(method);
//			if (annotation == null) {
//				return null;
//			}
//			StringTokenizer strTok = new StringTokenizer(annotation.getArgumentNames(), ",");
//			if (strTok.countTokens() > 0) {
//				String[] names = new String[strTok.countTokens()];
//				for (int i = 0; i < names.length; i++) {
//					names[i] = strTok.nextToken();
//				}
//				return names;
//			}
//			else {
//				return null;
//			}
//		}
//
//		@Override
//		@Nullable
//		public String[] getParameterNames(Constructor<?> ctor) {
//			throw new UnsupportedOperationException("Spring AOP cannot handle constructor advice");
//		}
//	}

}