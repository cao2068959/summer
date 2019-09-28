package com.chy.summer.framework.aop.aspectj.annotation;

import com.chy.summer.framework.aop.Pointcut;
import com.chy.summer.framework.aop.aspectj.AspectJExpressionPointcut;
import com.chy.summer.framework.aop.aspectj.TypePatternClassFilter;
import com.chy.summer.framework.aop.framework.AopConfigException;
import com.chy.summer.framework.aop.support.ComposablePointcut;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PerClauseKind;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * AspectJ切面类的元数据，以及切点表达式
 */
public class AspectMetadata implements Serializable {

	/**
	 * summer为这个切面定义的名称
	 */
	private final String aspectName;

	/**
	 * 切面的类型，
	 * 以便在反序列化时重新解析对应的AjType
	 */
	private final Class<?> aspectClass;

	/**
	 * AspectJ反射信息
	 * 由于AspectJ本身是不可序列化的，当需要反序列的时候通过这个解析
	 */
	private transient AjType<?> ajType;

	/**
	 * summer AOP切入点对应于切面的表达式。
	 * 切入点必须是单例的、规范的真正实例。
	 */
	private final Pointcut perClausePointcut;


	/**
	 * 为给定的切面类创建一个新的AspectMetadata实例。
	 * @param aspectClass 切面类
	 * @param aspectName 切面的名称
	 */
	public AspectMetadata(Class<?> aspectClass, String aspectName) {
		//设置切面名称
		this.aspectName = aspectName;

		Class<?> currClass = aspectClass;
		AjType<?> ajType = null;
		//这里循环查找 带有Aspect的类，一直找到父类为Object
		while (currClass != Object.class) {
			AjType<?> ajTypeToCheck = AjTypeSystem.getAjType(currClass);
			if (ajTypeToCheck.isAspect()) {
				//这里的AjType所持有的aspectClass为带有@Aspect注解的类
				ajType = ajTypeToCheck;
				break;
			}
			//获取类型的父类
			currClass = currClass.getSuperclass();
		}
		//没有获取到带有@Aspect注解的类
		if (ajType == null) {
			throw new IllegalArgumentException(aspectClass.getName() + "类不是一个@Aspect注解的类");
		}
		if (ajType.getDeclarePrecedence().length > 0) {
			throw new IllegalArgumentException("目前不支持DeclarePrecendence");
		}
		//设置切面的类型
		this.aspectClass = ajType.getJavaClass();
		//设置AspectJ反射信息
		this.ajType = ajType;
		//判断切面表达式的类型
		switch (this.ajType.getPerClause().getKind()) {
			case SINGLETON:
				this.perClausePointcut = Pointcut.TRUE;
				return;
			case PERTARGET:
			case PERTHIS:
				AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut();
				ajexp.setLocation(aspectClass.getName());
				ajexp.setExpression(findPerClause(aspectClass));
				ajexp.setPointcutDeclarationScope(aspectClass);
				this.perClausePointcut = ajexp;
				return;
			case PERTYPEWITHIN:
				this.perClausePointcut = new ComposablePointcut(new TypePatternClassFilter(findPerClause(aspectClass)));
				return;
			default:
				throw new AopConfigException(
						"目前不支持"+aspectClass+"类中的切点表达式："+ajType.getPerClause().getKind());
		}
	}

	/**
	 * 从切面对象中提取接入点表达式
	 */
	private String findPerClause(Class<?> aspectClass) {
		String str = aspectClass.getAnnotation(Aspect.class).value();
		str = str.substring(str.indexOf("(") + 1);
		str = str.substring(0, str.length() - 1);
		return str;
	}


	/**
	 * 获取AspectJ反射信息
	 */
	public AjType<?> getAjType() {
		return this.ajType;
	}

	/**
	 * 获取切面对象
	 */
	public Class<?> getAspectClass() {
		return this.aspectClass;
	}

	/**
	 * 获取切面对象名称
	 */
	public String getAspectName() {
		return this.aspectName;
	}

	/**
	 * 获取切面的切入点表达式
	 * （单例的情况下）
	 */
	public Pointcut getPerClausePointcut() {
		return this.perClausePointcut;
	}

	/**
	 * 获取切面定义为“perthis”还是“pertarget”。
	 */
	public boolean isPerThisOrPerTarget() {
		PerClauseKind kind = getAjType().getPerClause().getKind();
		return (kind == PerClauseKind.PERTARGET || kind == PerClauseKind.PERTHIS);
	}

	/**
	 * 获取切面是否定义为“pertypewithin”。
	 */
	public boolean isPerTypeWithin() {
		PerClauseKind kind = getAjType().getPerClause().getKind();
		return (kind == PerClauseKind.PERTYPEWITHIN);
	}

	/**
	 * 是否需要延迟实例化切面。
	 */
	public boolean isLazilyInstantiated() {
		return (isPerThisOrPerTarget() || isPerTypeWithin());
	}

	/**
	 * 读取AspectJ反射信息
	 */
	private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		this.ajType = AjTypeSystem.getAjType(this.aspectClass);
	}

}