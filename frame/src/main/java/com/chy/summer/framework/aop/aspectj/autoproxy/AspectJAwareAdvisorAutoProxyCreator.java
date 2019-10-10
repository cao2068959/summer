package com.chy.summer.framework.aop.aspectj.autoproxy;

import com.chy.summer.framework.aop.Advisor;
import com.chy.summer.framework.aop.aopalliance.Advice;
import com.chy.summer.framework.aop.aspectj.AbstractAspectJAdvice;
import com.chy.summer.framework.aop.aspectj.AspectJPointcutAdvisor;
import com.chy.summer.framework.aop.aspectj.AspectJProxyUtils;
import com.chy.summer.framework.aop.framework.autoProxy.AbstractAdvisorAutoProxyCreator;
import com.chy.summer.framework.core.ordered.Ordered;
import com.chy.summer.framework.util.ClassUtils;
import org.aspectj.util.PartialOrder;
import org.aspectj.util.PartialOrder.PartialComparable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 通用的自动代理创建器，它为每个bean检测到的Advisor为特定bean构建AOP代理
 *
 * 子类可以覆盖findCandidateAdvisors()方法来返回应用于任何对象的自定义Advisor列表
 * 子类还可以覆盖继承的AbstractAutoProxyCreator.shouldSkip(java.lang.Class方法来排除某些对象的自动代理
 *
 * 需要排序的advisor或advice应实现order的接口
 * 该类按有序顺序值对advisor排序
 * 未实现有序接口的顾问将被视为无序的;它们将以未定义的顺序出现在advisor链的末端
 */
public class AspectJAwareAdvisorAutoProxyCreator extends AbstractAdvisorAutoProxyCreator {

	private static final Comparator<Advisor> DEFAULT_PRECEDENCE_COMPARATOR = new AspectJPrecedenceComparator();


	/**
	 * 按照AspectJ优先级对其余部分排序
     * 如果两条advice来自同一切面，它们的顺序将是相同的。
     * 然后根据下列规则进一步排列来自同一切面的advice:
	 * 如果其中一个位是后置通知，那么最后声明的通知的优先级将变高(提前运行)，否则，首先声明的通知优先级最高(先运行)
	 * advisor按优先级排序，从数值最大的优先级越低。“在进入”到连接点的过程中，应该先运行优先级最高的advisor
     * 在连接点的“退出”过程中，优先级最高的advisor应该最后运行。
	 */
	@Override
	protected List<Advisor> sortAdvisors(List<Advisor> advisors) {
		List<PartiallyComparableAdvisorHolder> partiallyComparableAdvisors =
				new ArrayList<>(advisors.size());
		for (Advisor element : advisors) {
		    //为每一个advisor添加排序器
			partiallyComparableAdvisors.add(
					new PartiallyComparableAdvisorHolder(element, DEFAULT_PRECEDENCE_COMPARATOR));
		}
		//执行排序
		List<PartiallyComparableAdvisorHolder> sorted =
				PartialOrder.sort(partiallyComparableAdvisors);
		if (sorted != null) {
		    //将排序好的advisor按顺序取出
			List<Advisor> result = new ArrayList<>(advisors.size());
			for (PartiallyComparableAdvisorHolder pcAdvisor : sorted) {
				result.add(pcAdvisor.getAdvisor());
			}
			return result;
		}
		else {
		    //排序失败尝试适用，自带的list排序规则进行排序，或者使用码农自定义的排序方式
			return super.sortAdvisors(advisors);
		}
	}

	/**
	 * 在advice链的开头添加一个说明性调用拦截器（ExposeInvocationInterceptor )
     * 在使用AspectJ表达式切入点和使用AspectJ形式的advice时，需要这些额外的advice
	 */
	@Override
	protected void extendAdvisors(List<Advisor> candidateAdvisors) {
	    //添加特殊顾问
		AspectJProxyUtils.makeAdvisorChainAspectJCapableIfNecessary(candidateAdvisors);
	}

    /**
     * 判断是否跳过这个bean
     */
	@Override
	protected boolean shouldSkip(Class<?> beanClass, String beanName) {
	    //所有可以在自动代理中使用的候选Advisor
		List<Advisor> candidateAdvisors = findCandidateAdvisors();
		for (Advisor advisor : candidateAdvisors) {
			if (advisor instanceof AspectJPointcutAdvisor) {
			    //跳过AspectJAdvice
				if (((AbstractAspectJAdvice) advisor.getAdvice()).getAspectName().equals(beanName)) {
					return true;
				}
			}
		}
		//这里返回的false就是不跳过
		return super.shouldSkip(beanClass, beanName);
	}


	/**
	 * 实现AspectJ PartialComparable接口来定义部分排序
	 */
	private static class PartiallyComparableAdvisorHolder implements PartialComparable {
        /**
         * 顾问
         */
		private final Advisor advisor;

        /**
         * 优先级比较器
         */
		private final Comparator<Advisor> comparator;

		public PartiallyComparableAdvisorHolder(Advisor advisor, Comparator<Advisor> comparator) {
			this.advisor = advisor;
			this.comparator = comparator;
		}

        /**
         * 进行优先级比较
         * @param obj 比对的对象
         */
		@Override
		public int compareTo(Object obj) {
			Advisor otherAdvisor = ((PartiallyComparableAdvisorHolder) obj).advisor;
			return this.comparator.compare(this.advisor, otherAdvisor);
		}

		@Override
		public int fallbackCompareTo(Object obj) {
			return 0;
		}

		public Advisor getAdvisor() {
			return this.advisor;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			Advice advice = this.advisor.getAdvice();
			sb.append(ClassUtils.getShortName(advice.getClass()));
			sb.append(": ");
			if (this.advisor instanceof Ordered) {
				sb.append("order ").append(((Ordered) this.advisor).getOrder()).append(", ");
			}
			if (advice instanceof AbstractAspectJAdvice) {
				AbstractAspectJAdvice ajAdvice = (AbstractAspectJAdvice) advice;
				sb.append(ajAdvice.getAspectName());
				sb.append(", declaration order ");
				sb.append(ajAdvice.getDeclarationOrder());
			}
			return sb.toString();
		}
	}

}