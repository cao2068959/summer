package com.chy.summer.framework.aop.framework.autoProxy;

import com.chy.summer.framework.aop.TargetSource;
import com.chy.summer.framework.beans.BeanFactory;
import com.chy.summer.framework.beans.FactoryBean;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.PatternMatchUtils;
import com.chy.summer.framework.util.StringUtils;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动代理创建器，用于通过名称列表标识要代理的bean
 */
public class BeanNameAutoProxyCreator extends AbstractAutoProxyCreator {

	@Nullable
	private List<String> beanNames;


	/**
	 * 设置应自动用代理包装的bean的名称
	 * 名称可以以“*”结尾来指定要匹配的前缀，例如 “ myBean,tx *”将匹配名为“myBean”的bean和所有名称以“tx”开头的bean
	 */
	public void setBeanNames(String... beanNames) {
		Assert.notEmpty(beanNames, "'beanNames'不可为空");
		this.beanNames = new ArrayList<>(beanNames.length);
		for (String mappedName : beanNames) {
			//添加到bean名称的缓存里
			this.beanNames.add(StringUtils.trimWhitespace(mappedName));
		}
	}


	/**
	 * 如果Bean名称在已配置的名称列表中，则标识为要代理的Bean
	 */
	@Override
	@Nullable
	protected Object[] getAdvicesAndAdvisorsForBean(
			Class<?> beanClass, String beanName, @Nullable TargetSource targetSource) {

		if (this.beanNames != null) {
			for (String mappedName : this.beanNames) {
				if (FactoryBean.class.isAssignableFrom(beanClass)) {
					//通过标识符 判断是不是工厂bean
					if (!mappedName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
						continue;
					}
					//获取工厂bean的名称 ，一般情况下就是前面多了个&
					mappedName = mappedName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
				}
				if (isMatch(beanName, mappedName)) {
					//没有其他额外的代理
					return PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS;
				}
				//获取持有的bean工厂
//				BeanFactory beanFactory = getBeanFactory();
//				if (beanFactory != null) {
//					String[] aliases = beanFactory.getAliases(beanName);
//					for (String alias : aliases) {
//						if (isMatch(alias, mappedName)) {
//							//没有其他额外的代理
//							return PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS;
//						}
//					}
//				}
			}
		}
		//不需要代理
		return DO_NOT_PROXY;
	}

	/**
	 * 返回给定的bean名称与映射的名称是否匹配
	 * 默认实现检查“ xxx *”，“ * xxx”和“ * xxx *”匹配项以及直接相等性。 可以在子类中覆盖
	 * @param beanName 需要检查的bean名称
	 * @param mappedName 设置在名称列表中的名称
	 */
	protected boolean isMatch(String beanName, String mappedName) {
		return PatternMatchUtils.simpleMatch(mappedName, beanName);
	}

}