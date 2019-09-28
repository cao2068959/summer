package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.aop.ClassFilter;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.StringUtils;
import com.sun.istack.internal.Nullable;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.TypePatternMatcher;

/**
 * 使用AspectJ类型匹配实现ClassFilter
 */
public class TypePatternClassFilter implements ClassFilter {

	/**
	 * AspectJ解析的类型范围
	 * 例如：com.chy.summer.framework.*
	 * 例如：com.chy.summer.framework.beans.ITestBean+
	 */
	private String typePattern = "";

	@Nullable
	private TypePatternMatcher aspectJTypePatternMatcher;


	/**
	 * 创建TypePatternClassFilter类的新实例
	 * 一定要设置typePattern属性，否则在第一次调用matches(Class)方法时，无疑会抛出一个致命的IllegalStateException。
	 */
	public TypePatternClassFilter() {
	}

	/**
	 * 使用给定的类型范围创建完全配置的TypePatternClassFilter
	 */
	public TypePatternClassFilter(String typePattern) {
		setTypePattern(typePattern);
	}


	/**
	 * 设置匹配的类型范围
	 * 例如：com.chy.summer.framework.*
	 * 这将匹配给定包中的任何类或接口
	 * 例如：com.chy.summer.framework.beans.ITestBean+
	 * 这将匹配ITestBean接口和实现它的任何类。
	 * 这些由AspectJ约定
	 * @param typePattern AspectJ解析的类型范围
	 */
	public void setTypePattern(String typePattern) {
		Assert.notNull(typePattern, "匹配的类型范围不能为空");
		this.typePattern = typePattern;
		this.aspectJTypePatternMatcher =
				PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution().
				parseTypePattern(replaceBooleanOperators(typePattern));
	}

	/**
	 * 获取AspectJ解析的类型范围
	 */
	public String getTypePattern() {
		return this.typePattern;
	}


	/**
	 * 判断切入点是否用于给定的接口或目标类
	 * @param clazz 给定的目标类
	 */
	@Override
	public boolean matches(Class<?> clazz) {
		Assert.state(this.aspectJTypePatternMatcher != null, "没有设置类型范围");
		return this.aspectJTypePatternMatcher.matches(clazz);
	}

	/**
	 * 如果在XML中指定了类型范围，则用户不能将"and"写成“&&”
	 */
	private String replaceBooleanOperators(String pcExpr) {
		String result = StringUtils.replace(pcExpr," and "," && ");
		result = StringUtils.replace(result, " or ", " || ");
		return StringUtils.replace(result, " not ", " ! ");
	}
}