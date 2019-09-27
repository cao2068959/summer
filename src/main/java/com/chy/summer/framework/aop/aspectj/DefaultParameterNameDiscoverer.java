package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.core.PrioritizedParameterNameDiscoverer;
import com.chy.summer.framework.core.StandardReflectionParameterNameDiscoverer;

/**
 * 参数名发现器，用于参数绑定
 */
public class DefaultParameterNameDiscoverer extends PrioritizedParameterNameDiscoverer {

	public DefaultParameterNameDiscoverer() {
		//反射的方式参数名发现器
		addDiscoverer(new StandardReflectionParameterNameDiscoverer());
		//TODO GYX 这里差一个ASM的参数发现器
		//Asm的方式参数名发现器
//		addDiscoverer(new LocalVariableTableParameterNameDiscoverer());
	}

}