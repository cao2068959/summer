package com.chy.summer.framework.aop.aspectj.autoproxy;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.aspectj.AspectJAopUtils;
import com.chy.summer.framework.aop.aspectj.AspectJPrecedenceInformation;
import com.chy.summer.framework.core.ordered.AnnotationAwareOrderComparator;
import com.chy.summer.framework.util.Assert;

import java.util.Comparator;

/**
 * AspectJ advice/advisor的优先级排序器
 */
class AspectJPrecedenceComparator implements Comparator<Advisor> {

	private static final int HIGHER_PRECEDENCE = -1;

	private static final int SAME_PRECEDENCE = 0;

	private static final int LOWER_PRECEDENCE = 1;


	private final Comparator<? super Advisor> advisorComparator;


	/**
	 * 创建一个默认的AspectJPrecedenceComparator.
	 */
	public AspectJPrecedenceComparator() {
		this.advisorComparator = AnnotationAwareOrderComparator.INSTANCE;
	}

	/**
	 * 使用给定的比较器来比较Advisor实例，创建AspectJPrecedenceComparator
	 * @param advisorComparator 用于Advisor的比较器
	 */
	public AspectJPrecedenceComparator(Comparator<? super Advisor> advisorComparator) {
		Assert.notNull(advisorComparator, "Advisor不可为空");
		this.advisorComparator = advisorComparator;
	}

	/**
	 * 重写对比方法
	 */
	@Override
	public int compare(Advisor o1, Advisor o2) {
		int advisorPrecedence = this.advisorComparator.compare(o1, o2);
		//两个对象相同，并且在同一个切面中
		if (advisorPrecedence == SAME_PRECEDENCE && declaredInSameAspect(o1, o2)) {
			advisorPrecedence = comparePrecedenceWithinAspect(o1, o2);
		}
		return advisorPrecedence;
	}

	/**
	 * 对比两个方法的优先级
	 */
	private int comparePrecedenceWithinAspect(Advisor advisor1, Advisor advisor2) {
		//如果advisors是一种事后通知,返回true
		boolean oneOrOtherIsAfterAdvice =
				(AspectJAopUtils.isAfterAdvice(advisor1) || AspectJAopUtils.isAfterAdvice(advisor2));
		//获取优先度，比较优先度
		int adviceDeclarationOrderDelta = getAspectDeclarationOrder(advisor1) - getAspectDeclarationOrder(advisor2);
		//后置通知的时候，后声明的advice有更高的优先级
		if (oneOrOtherIsAfterAdvice) {
			// 最后声明的advice具有更高的优先级
			if (adviceDeclarationOrderDelta < 0) {
				// 在advice2之前声明advice1 ，advice1的优先级较低
				return LOWER_PRECEDENCE;
			}
			else if (adviceDeclarationOrderDelta == 0) {
				return SAME_PRECEDENCE;
			}
			else {
				return HIGHER_PRECEDENCE;
			}
		}
		else {
			// 先声明的advice具有更高的优先级
			if (adviceDeclarationOrderDelta < 0) {
				// 在advice2之前声明advice1 ，advice1的优先级较高
				return HIGHER_PRECEDENCE;
			}
			else if (adviceDeclarationOrderDelta == 0) {
				return SAME_PRECEDENCE;
			}
			else {
				return LOWER_PRECEDENCE;
			}
		}
	}

	/**
	 * 判断是否在同一个切面中
	 */
	private boolean declaredInSameAspect(Advisor advisor1, Advisor advisor2) {
		return (hasAspectName(advisor1) && hasAspectName(advisor2) &&
				getAspectName(advisor1).equals(getAspectName(advisor2)));
	}

	/**
	 * 判断有没有AspectName
	 */
	private boolean hasAspectName(Advisor anAdvisor) {
		return (anAdvisor instanceof AspectJPrecedenceInformation ||
				anAdvisor.getAdvice() instanceof AspectJPrecedenceInformation);
	}

	/**
	 *
	 * 有AspectName的时候才可以使用
	 */
	private String getAspectName(Advisor anAdvisor) {
		//获取优先级信息
		AspectJPrecedenceInformation pi = AspectJAopUtils.getAspectJPrecedenceInformationFor(anAdvisor);
		Assert.state(pi != null, "无法解析的优先级信息");
		return pi.getAspectName();
	}

	/**
	 * 获取切面声明顺序
	 */
	private int getAspectDeclarationOrder(Advisor anAdvisor) {
		//获取advice/advisors的优先顺序信息
		AspectJPrecedenceInformation precedenceInfo =
			AspectJAopUtils.getAspectJPrecedenceInformationFor(anAdvisor);
		if (precedenceInfo != null) {
			//获取切面的优先度的数值
			return precedenceInfo.getDeclarationOrder();
		}
		else {
			return 0;
		}
	}

}