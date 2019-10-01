package com.chy.summer.framework.core.cglib;

import net.sf.cglib.core.DefaultNamingPolicy;

//Summer的cglib代理的类的命名规则
public class SummerNamingPolicy extends DefaultNamingPolicy {

	public static final SummerNamingPolicy INSTANCE = new SummerNamingPolicy();

	@Override
	protected String getTag() {
		return "BySummerCGLIB";
	}

}