package com.chy.summer.framework.exception;

import com.chy.summer.framework.util.StringUtils;
import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;

public class NoUniqueBeanDefinitionException extends NoSuchBeanDefinitionException {

	private int numberOfBeansFound;

	@Nullable
	private Collection<String> beanNamesFound;

	public NoUniqueBeanDefinitionException(String message) {
		super(message);
	}


	public NoUniqueBeanDefinitionException(String format, Object... param) {
		super(format, param);
	}

	/**
	 * Create a new {@code NoUniqueBeanDefinitionException}.
	 * @param type required type of the non-unique bean
	 * @param numberOfBeansFound the number of matching beans
	 * @param message detailed message describing the problem
	 */
	public NoUniqueBeanDefinitionException(Class<?> type, int numberOfBeansFound, String message) {
//		super(type, message);
		super(message);
		this.numberOfBeansFound = numberOfBeansFound;
	}

	/**
	 * Create a new {@code NoUniqueBeanDefinitionException}.
	 * @param type required type of the non-unique bean
	 * @param beanNamesFound the names of all matching beans (as a Collection)
	 */
	public NoUniqueBeanDefinitionException(Class<?> type, Collection<String> beanNamesFound) {
		this(type, beanNamesFound.size(), "expected single matching bean but found " + beanNamesFound.size() + ": " +
				StringUtils.collectionToCommaDelimitedString(beanNamesFound));
		this.beanNamesFound = beanNamesFound;
	}

	/**
	 * Create a new {@code NoUniqueBeanDefinitionException}.
	 * @param type required type of the non-unique bean
	 * @param beanNamesFound the names of all matching beans (as an array)
	 */
	public NoUniqueBeanDefinitionException(Class<?> type, String... beanNamesFound) {
		this(type, Arrays.asList(beanNamesFound));
	}


	/**
	 * Return the number of beans found when only one matching bean was expected.
	 * For a NoUniqueBeanDefinitionException, this will usually be higher than 1.
	 */
	public int getNumberOfBeansFound() {
		return this.numberOfBeansFound;
	}

	/**
	 * Return the names of all beans found when only one matching bean was expected.
	 * Note that this may be {@code null} if not specified at construction time.
	 */
	@Nullable
	public Collection<String> getBeanNamesFound() {
		return this.beanNamesFound;
	}

}
