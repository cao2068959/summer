package com.chy.summer.framework.aop.framework.autoProxy;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.beans.config.ConfigurableListableBeanFactory;
import com.chy.summer.framework.exception.BeanCreationException;
import com.chy.summer.framework.exception.BeanCurrentlyInCreationException;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.BeanFactoryUtils;
import com.sun.istack.internal.Nullable;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

/**
 * 在自动代理时,用于从BeanFactory检索标准的Advisor的帮助类
 */
@Slf4j
public class BeanFactoryAdvisorRetrievalHelper {

	private final ConfigurableListableBeanFactory beanFactory;

	@Nullable
	private String[] cachedAdvisorBeanNames;


	/**
	 * 为给定的BeanFactory创建一个新的BeanFactoryAdvisorRetrievalHelper
	 */
	public BeanFactoryAdvisorRetrievalHelper(ConfigurableListableBeanFactory beanFactory) {
		Assert.notNull(beanFactory, "ListableBeanFactory不可为空");
		this.beanFactory = beanFactory;
	}


	/**
	 * 在当前bean工厂中查找所有合格的Advisor bean，忽略FactoryBeans并排除当前正在创建的bean
	 */
	public List<Advisor> findAdvisorBeans() {
		// 获取顾问程序bean名称的列表
		String[] advisorNames = null;
		synchronized (this) {
			//从缓存中获取
//			advisorNames = this.cachedAdvisorBeanNames;
			if (advisorNames == null) {
				//获取Advisor类型的所有bean名称
				advisorNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
						this.beanFactory, Advisor.class, true, false);
				this.cachedAdvisorBeanNames = advisorNames;
			}
		}
		//没有获取到advisorNames
		if (advisorNames.length == 0) {
			return new LinkedList<>();
		}

		List<Advisor> advisors = new LinkedList<>();
		for (String name : advisorNames) {
			//判断bean是否合格
			if (isEligibleBean(name)) {
				//判断是个bean是否在创建中
				if (this.beanFactory.isCurrentlyInCreation(name)) {
					if (log.isDebugEnabled()) {
						log.debug("跳过当前创建的advisor '" + name + "'");
					}
				}
				else {
					try {
						//获取bean，添加到列表中
						advisors.add(this.beanFactory.getBean(name, Advisor.class));
					}
					catch (BeanCreationException ex) {
						Throwable rootCause = ex.getMostSpecificCause();
						if (rootCause instanceof BeanCurrentlyInCreationException) {
							BeanCreationException bce = (BeanCreationException) rootCause;
							String bceBeanName = bce.getBeanName();
							if (bceBeanName != null && this.beanFactory.isCurrentlyInCreation(bceBeanName)) {
								if (log.isDebugEnabled()) {
									log.debug("跳过advisor '" + name +
											"' 依赖于当前创建的bean: " + ex.getMessage());
								}
								//继续查询其他的advisor
								continue;
							}
						}
						throw ex;
					}
				}
			}
		}
		return advisors;
	}

	/**
	 * 判断bean是否合格
	 * 默认实现始终为true
	 */
	protected boolean isEligibleBean(String beanName) {
		return true;
	}

}