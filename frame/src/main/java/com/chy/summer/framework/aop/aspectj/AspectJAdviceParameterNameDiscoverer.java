package com.chy.summer.framework.aop.aspectj;

import com.chy.summer.framework.core.ParameterNameDiscoverer;
import com.chy.summer.framework.util.StringUtils;
import javax.annotation.Nullable;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AspectJAdviceParameterNameDiscoverer implements ParameterNameDiscoverer {
	//TODO GYX 没有完成
	private static final String THIS_JOIN_POINT = "thisJoinPoint";
	private static final String THIS_JOIN_POINT_STATIC_PART = "thisJoinPointStaticPart";

	// Steps in the binding algorithm...
	private static final int STEP_JOIN_POINT_BINDING = 1;
	private static final int STEP_THROWING_BINDING = 2;
	private static final int STEP_ANNOTATION_BINDING = 3;
	private static final int STEP_RETURNING_BINDING = 4;
	private static final int STEP_PRIMITIVE_ARGS_BINDING = 5;
	private static final int STEP_THIS_TARGET_ARGS_BINDING = 6;
	private static final int STEP_REFERENCE_PCUT_BINDING = 7;
	private static final int STEP_FINISHED = 8;

	private static final Set<String> singleValuedAnnotationPcds = new HashSet<>();
	private static final Set<String> nonReferencePointcutTokens = new HashSet<>();


	static {
		singleValuedAnnotationPcds.add("@this");
		singleValuedAnnotationPcds.add("@target");
		singleValuedAnnotationPcds.add("@within");
		singleValuedAnnotationPcds.add("@withincode");
		singleValuedAnnotationPcds.add("@annotation");

		Set<PointcutPrimitive> pointcutPrimitives = PointcutParser.getAllSupportedPointcutPrimitives();
		for (PointcutPrimitive primitive : pointcutPrimitives) {
			nonReferencePointcutTokens.add(primitive.getName());
		}
		nonReferencePointcutTokens.add("&&");
		nonReferencePointcutTokens.add("!");
		nonReferencePointcutTokens.add("||");
		nonReferencePointcutTokens.add("and");
		nonReferencePointcutTokens.add("or");
		nonReferencePointcutTokens.add("not");
	}


	/** The pointcut expression associated with the advice, as a simple String */
	@Nullable
	private String pointcutExpression;

	private boolean raiseExceptions;

	/** If the advice is afterReturning, and binds the return value, this is the parameter name used */
	@Nullable
	private String returningName;

	/** If the advice is afterThrowing, and binds the thrown value, this is the parameter name used */
	@Nullable
	private String throwingName;

	private Class<?>[] argumentTypes = new Class<?>[0];

	private String[] parameterNameBindings = new String[0];

	private int numberOfRemainingUnboundArguments;


	/**
	 * Create a new discoverer that attempts to discover parameter names
	 * from the given pointcut expression.
	 */
	public AspectJAdviceParameterNameDiscoverer(@Nullable String pointcutExpression) {
		this.pointcutExpression = pointcutExpression;
	}


	/**
	 * Indicate whether {@link IllegalArgumentException} and {@link AmbiguousBindingException}
	 * must be thrown as appropriate in the case of failing to deduce advice parameter names.
	 * @param raiseExceptions {@code true} if exceptions are to be thrown
	 */
	public void setRaiseExceptions(boolean raiseExceptions) {
		this.raiseExceptions = raiseExceptions;
	}

	/**
	 * If {@code afterReturning} advice binds the return value, the
	 * returning variable name must be specified.
	 * @param returningName the name of the returning variable
	 */
	public void setReturningName(@Nullable String returningName) {
		this.returningName = returningName;
	}

	/**
	 * If {@code afterThrowing} advice binds the thrown value, the
	 * throwing variable name must be specified.
	 * @param throwingName the name of the throwing variable
	 */
	public void setThrowingName(@Nullable String throwingName) {
		this.throwingName = throwingName;
	}


	/**
	 * Deduce the parameter names for an advice method.
	 * <p>See the {@link AspectJAdviceParameterNameDiscoverer class level javadoc}
	 * for this class for details of the algorithm used.
	 * @param method the target {@link Method}
	 * @return the parameter names
	 */
	@Override
	@Nullable
	public String[] getParameterNames(Method method) {
		this.argumentTypes = method.getParameterTypes();
		this.numberOfRemainingUnboundArguments = this.argumentTypes.length;
		this.parameterNameBindings = new String[this.numberOfRemainingUnboundArguments];

		int minimumNumberUnboundArgs = 0;
		if (this.returningName != null) {
			minimumNumberUnboundArgs++;
		}
		if (this.throwingName != null) {
			minimumNumberUnboundArgs++;
		}
		if (this.numberOfRemainingUnboundArguments < minimumNumberUnboundArgs) {
			throw new IllegalStateException(
					"Not enough arguments in method to satisfy binding of returning and throwing variables");
		}

		try {
			int algorithmicStep = STEP_JOIN_POINT_BINDING;
			while ((this.numberOfRemainingUnboundArguments > 0) && algorithmicStep < STEP_FINISHED) {
				switch (algorithmicStep++) {
					case STEP_JOIN_POINT_BINDING:
						if (!maybeBindThisJoinPoint()) {
							maybeBindThisJoinPointStaticPart();
						}
						break;
					case STEP_THROWING_BINDING:
						maybeBindThrowingVariable();
						break;
					case STEP_ANNOTATION_BINDING:
						maybeBindAnnotationsFromPointcutExpression();
						break;
					case STEP_RETURNING_BINDING:
						maybeBindReturningVariable();
						break;
					case STEP_PRIMITIVE_ARGS_BINDING:
						maybeBindPrimitiveArgsFromPointcutExpression();
						break;
					case STEP_THIS_TARGET_ARGS_BINDING:
						maybeBindThisOrTargetOrArgsFromPointcutExpression();
						break;
					case STEP_REFERENCE_PCUT_BINDING:
						maybeBindReferencePointcutParameter();
						break;
					default:
						throw new IllegalStateException("Unknown algorithmic step: " + (algorithmicStep - 1));
				}
			}
		}
		catch (AmbiguousBindingException ambigEx) {
			if (this.raiseExceptions) {
				throw ambigEx;
			}
			else {
				return null;
			}
		}
		catch (IllegalArgumentException ex) {
			if (this.raiseExceptions) {
				throw ex;
			}
			else {
				return null;
			}
		}

		if (this.numberOfRemainingUnboundArguments == 0) {
			return this.parameterNameBindings;
		}
		else {
			if (this.raiseExceptions) {
				throw new IllegalStateException("Failed to bind all argument names: " +
						this.numberOfRemainingUnboundArguments + " argument(s) could not be bound");
			}
			else {
				// convention for failing is to return null, allowing participation in a chain of responsibility
				return null;
			}
		}
	}

	/**
	 * An advice method can never be a constructor in Spring.
	 * @return {@code null}
	 * @throws UnsupportedOperationException if
	 * {@link #setRaiseExceptions(boolean) raiseExceptions} has been set to {@code true}
	 */
	@Override
	@Nullable
	public String[] getParameterNames(Constructor<?> ctor) {
		if (this.raiseExceptions) {
			throw new UnsupportedOperationException("An advice method can never be a constructor");
		}
		else {
			// we return null rather than throw an exception so that we behave well
			// in a chain-of-responsibility.
			return null;
		}
	}


	private void bindParameterName(int index, String name) {
		this.parameterNameBindings[index] = name;
		this.numberOfRemainingUnboundArguments--;
	}

	/**
	 * If the first parameter is of type JoinPoint or ProceedingJoinPoint,bind "thisJoinPoint" as
	 * parameter name and return true, else return false.
	 */
	private boolean maybeBindThisJoinPoint() {
		if ((this.argumentTypes[0] == JoinPoint.class) || (this.argumentTypes[0] == ProceedingJoinPoint.class)) {
			bindParameterName(0, THIS_JOIN_POINT);
			return true;
		}
		else {
			return false;
		}
	}

	private void maybeBindThisJoinPointStaticPart() {
		if (this.argumentTypes[0] == JoinPoint.StaticPart.class) {
			bindParameterName(0, THIS_JOIN_POINT_STATIC_PART);
		}
	}

	/**
	 * If a throwing name was specified and there is exactly one choice remaining
	 * (argument that is a subtype of Throwable) then bind it.
	 */
	private void maybeBindThrowingVariable() {
		if (this.throwingName == null) {
			return;
		}

		// So there is binding work to do...
		int throwableIndex = -1;
		for (int i = 0; i < this.argumentTypes.length; i++) {
			if (isUnbound(i) && isSubtypeOf(Throwable.class, i)) {
				if (throwableIndex == -1) {
					throwableIndex = i;
				}
				else {
					// Second candidate we've found - ambiguous binding
					throw new AmbiguousBindingException("Binding of throwing parameter '" +
							this.throwingName + "' is ambiguous: could be bound to argument " +
							throwableIndex + " or argument " + i);
				}
			}
		}

		if (throwableIndex == -1) {
			throw new IllegalStateException("Binding of throwing parameter '" + this.throwingName
					+ "' could not be completed as no available arguments are a subtype of Throwable");
		}
		else {
			bindParameterName(throwableIndex, this.throwingName);
		}
	}

	/**
	 * If a returning variable was specified and there is only one choice remaining, bind it.
	 */
	private void maybeBindReturningVariable() {
		if (this.numberOfRemainingUnboundArguments == 0) {
			throw new IllegalStateException(
					"Algorithm assumes that there must be at least one unbound parameter on entry to this method");
		}

		if (this.returningName != null) {
			if (this.numberOfRemainingUnboundArguments > 1) {
				throw new AmbiguousBindingException("Binding of returning parameter '" + this.returningName +
						"' is ambiguous, there are " + this.numberOfRemainingUnboundArguments + " candidates.");
			}

			// We're all set... find the unbound parameter, and bind it.
			for (int i = 0; i < this.parameterNameBindings.length; i++) {
				if (this.parameterNameBindings[i] == null) {
					bindParameterName(i, this.returningName);
					break;
				}
			}
		}
	}


	/**
	 * Parse the string pointcut expression looking for:
	 * &#64;this, &#64;target, &#64;args, &#64;within, &#64;withincode, &#64;annotation.
	 * If we find one of these pointcut expressions, try and extract a candidate variable
	 * name (or variable names, in the case of args).
	 * <p>Some more support from AspectJ in doing this exercise would be nice... :)
	 */
	private void maybeBindAnnotationsFromPointcutExpression() {
		List<String> varNames = new ArrayList<>();
		String[] tokens = StringUtils.tokenizeToStringArray(this.pointcutExpression, " ");
		for (int i = 0; i < tokens.length; i++) {
			String toMatch = tokens[i];
			int firstParenIndex = toMatch.indexOf("(");
			if (firstParenIndex != -1) {
				toMatch = toMatch.substring(0, firstParenIndex);
			}
			if (singleValuedAnnotationPcds.contains(toMatch)) {
				PointcutBody body = getPointcutBody(tokens, i);
				i += body.numTokensConsumed;
				String varName = maybeExtractVariableName(body.text);
				if (varName != null) {
					varNames.add(varName);
				}
			}
			else if (tokens[i].startsWith("@args(") || tokens[i].equals("@args")) {
				PointcutBody body = getPointcutBody(tokens, i);
				i += body.numTokensConsumed;
				maybeExtractVariableNamesFromArgs(body.text, varNames);
			}
		}

		bindAnnotationsFromVarNames(varNames);
	}

	/**
	 * Match the given list of extracted variable names to argument slots.
	 */
	private void bindAnnotationsFromVarNames(List<String> varNames) {
		if (!varNames.isEmpty()) {
			// we have work to do...
			int numAnnotationSlots = countNumberOfUnboundAnnotationArguments();
			if (numAnnotationSlots > 1) {
				throw new AmbiguousBindingException("Found " + varNames.size() +
						" potential annotation variable(s), and " +
						numAnnotationSlots + " potential argument slots");
			}
			else if (numAnnotationSlots == 1) {
				if (varNames.size() == 1) {
					// it's a match
					findAndBind(Annotation.class, varNames.get(0));
				}
				else {
					// multiple candidate vars, but only one slot
					throw new IllegalArgumentException("Found " + varNames.size() +
							" candidate annotation binding variables" +
							" but only one potential argument binding slot");
				}
			}
			else {
				// no slots so presume those candidate vars were actually type names
			}
		}
	}

	/*
	 * If the token starts meets Java identifier conventions, it's in.
	 */
	@Nullable
	private String maybeExtractVariableName(@Nullable String candidateToken) {
		if (!StringUtils.hasLength(candidateToken)) {
			return null;
		}
		if (Character.isJavaIdentifierStart(candidateToken.charAt(0)) &&
				Character.isLowerCase(candidateToken.charAt(0))) {
			char[] tokenChars = candidateToken.toCharArray();
			for (char tokenChar : tokenChars) {
				if (!Character.isJavaIdentifierPart(tokenChar)) {
					return null;
				}
			}
			return candidateToken;
		}
		else {
			return null;
		}
	}

	/**
	 * Given an args pointcut body (could be {@code args} or {@code at_args}),
	 * add any candidate variable names to the given list.
	 */
	private void maybeExtractVariableNamesFromArgs(@Nullable String argsSpec, List<String> varNames) {
		if (argsSpec == null) {
			return;
		}
		String[] tokens = StringUtils.tokenizeToStringArray(argsSpec, ",");
		for (int i = 0; i < tokens.length; i++) {
			tokens[i] = StringUtils.trimWhitespace(tokens[i]);
			String varName = maybeExtractVariableName(tokens[i]);
			if (varName != null) {
				varNames.add(varName);
			}
		}
	}

	/**
	 * Parse the string pointcut expression looking for this(), target() and args() expressions.
	 * If we find one, try and extract a candidate variable name and bind it.
	 */
	private void maybeBindThisOrTargetOrArgsFromPointcutExpression() {
		if (this.numberOfRemainingUnboundArguments > 1) {
			throw new AmbiguousBindingException("Still " + this.numberOfRemainingUnboundArguments
					+ " unbound args at this(),target(),args() binding stage, with no way to determine between them");
		}

		List<String> varNames = new ArrayList<>();
		String[] tokens = StringUtils.tokenizeToStringArray(this.pointcutExpression, " ");
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals("this") ||
					tokens[i].startsWith("this(") ||
					tokens[i].equals("target") ||
					tokens[i].startsWith("target(")) {
				PointcutBody body = getPointcutBody(tokens, i);
				i += body.numTokensConsumed;
				String varName = maybeExtractVariableName(body.text);
				if (varName != null) {
					varNames.add(varName);
				}
			}
			else if (tokens[i].equals("args") || tokens[i].startsWith("args(")) {
				PointcutBody body = getPointcutBody(tokens, i);
				i += body.numTokensConsumed;
				List<String> candidateVarNames = new ArrayList<>();
				maybeExtractVariableNamesFromArgs(body.text, candidateVarNames);
				// we may have found some var names that were bound in previous primitive args binding step,
				// filter them out...
				for (String varName : candidateVarNames) {
					if (!alreadyBound(varName)) {
						varNames.add(varName);
					}
				}
			}
		}


		if (varNames.size() > 1) {
			throw new AmbiguousBindingException("Found " + varNames.size() +
					" candidate this(), target() or args() variables but only one unbound argument slot");
		}
		else if (varNames.size() == 1) {
			for (int j = 0; j < this.parameterNameBindings.length; j++) {
				if (isUnbound(j)) {
					bindParameterName(j, varNames.get(0));
					break;
				}
			}
		}
		// else varNames.size must be 0 and we have nothing to bind.
	}

	private void maybeBindReferencePointcutParameter() {
		if (this.numberOfRemainingUnboundArguments > 1) {
			throw new AmbiguousBindingException("Still " + this.numberOfRemainingUnboundArguments
					+ " unbound args at reference pointcut binding stage, with no way to determine between them");
		}

		List<String> varNames = new ArrayList<>();
		String[] tokens = StringUtils.tokenizeToStringArray(this.pointcutExpression, " ");
		for (int i = 0; i < tokens.length; i++) {
			String toMatch = tokens[i];
			if (toMatch.startsWith("!")) {
				toMatch = toMatch.substring(1);
			}
			int firstParenIndex = toMatch.indexOf("(");
			if (firstParenIndex != -1) {
				toMatch = toMatch.substring(0, firstParenIndex);
			}
			else {
				if (tokens.length < i + 2) {
					// no "(" and nothing following
					continue;
				}
				else {
					String nextToken = tokens[i + 1];
					if (nextToken.charAt(0) != '(') {
						// next token is not "(" either, can't be a pc...
						continue;
					}
				}

			}

			// eat the body
			PointcutBody body = getPointcutBody(tokens, i);
			i += body.numTokensConsumed;

			if (!nonReferencePointcutTokens.contains(toMatch)) {
				// then it could be a reference pointcut
				String varName = maybeExtractVariableName(body.text);
				if (varName != null) {
					varNames.add(varName);
				}
			}
		}

		if (varNames.size() > 1) {
			throw new AmbiguousBindingException("Found " + varNames.size() +
					" candidate reference pointcut variables but only one unbound argument slot");
		}
		else if (varNames.size() == 1) {
			for (int j = 0; j < this.parameterNameBindings.length; j++) {
				if (isUnbound(j)) {
					bindParameterName(j, varNames.get(0));
					break;
				}
			}
		}
		// else varNames.size must be 0 and we have nothing to bind.
	}

	/*
	 * We've found the start of a binding pointcut at the given index into the
	 * token array. Now we need to extract the pointcut body and return it.
	 */
	private PointcutBody getPointcutBody(String[] tokens, int startIndex) {
		int numTokensConsumed = 0;
		String currentToken = tokens[startIndex];
		int bodyStart = currentToken.indexOf('(');
		if (currentToken.charAt(currentToken.length() - 1) == ')') {
			// It's an all in one... get the text between the first (and the last)
			return new PointcutBody(0, currentToken.substring(bodyStart + 1, currentToken.length() - 1));
		}
		else {
			StringBuilder sb = new StringBuilder();
			if (bodyStart >= 0 && bodyStart != (currentToken.length() - 1)) {
				sb.append(currentToken.substring(bodyStart + 1));
				sb.append(" ");
			}
			numTokensConsumed++;
			int currentIndex = startIndex + numTokensConsumed;
			while (currentIndex < tokens.length) {
				if (tokens[currentIndex].equals("(")) {
					currentIndex++;
					continue;
				}

				if (tokens[currentIndex].endsWith(")")) {
					sb.append(tokens[currentIndex].substring(0, tokens[currentIndex].length() - 1));
					return new PointcutBody(numTokensConsumed, sb.toString().trim());
				}

				String toAppend = tokens[currentIndex];
				if (toAppend.startsWith("(")) {
					toAppend = toAppend.substring(1);
				}
				sb.append(toAppend);
				sb.append(" ");
				currentIndex++;
				numTokensConsumed++;
			}

		}

		// We looked and failed...
		return new PointcutBody(numTokensConsumed, null);
	}

	/**
	 * Match up args against unbound arguments of primitive types
	 */
	private void maybeBindPrimitiveArgsFromPointcutExpression() {
		int numUnboundPrimitives = countNumberOfUnboundPrimitiveArguments();
		if (numUnboundPrimitives > 1) {
			throw new AmbiguousBindingException("Found '" + numUnboundPrimitives +
					"' unbound primitive arguments with no way to distinguish between them.");
		}
		if (numUnboundPrimitives == 1) {
			// Look for arg variable and bind it if we find exactly one...
			List<String> varNames = new ArrayList<>();
			String[] tokens = StringUtils.tokenizeToStringArray(this.pointcutExpression, " ");
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].equals("args") || tokens[i].startsWith("args(")) {
					PointcutBody body = getPointcutBody(tokens, i);
					i += body.numTokensConsumed;
					maybeExtractVariableNamesFromArgs(body.text, varNames);
				}
			}
			if (varNames.size() > 1) {
				throw new AmbiguousBindingException("Found " + varNames.size() +
						" candidate variable names but only one candidate binding slot when matching primitive args");
			}
			else if (varNames.size() == 1) {
				// 1 primitive arg, and one candidate...
				for (int i = 0; i < this.argumentTypes.length; i++) {
					if (isUnbound(i) && this.argumentTypes[i].isPrimitive()) {
						bindParameterName(i, varNames.get(0));
						break;
					}
				}
			}
		}
	}

	/*
	 * Return true if the parameter name binding for the given parameter
	 * index has not yet been assigned.
	 */
	private boolean isUnbound(int i) {
		return this.parameterNameBindings[i] == null;
	}

	private boolean alreadyBound(String varName) {
		for (int i = 0; i < this.parameterNameBindings.length; i++) {
			if (!isUnbound(i) && varName.equals(this.parameterNameBindings[i])) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Return {@code true} if the given argument type is a subclass
	 * of the given supertype.
	 */
	private boolean isSubtypeOf(Class<?> supertype, int argumentNumber) {
		return supertype.isAssignableFrom(this.argumentTypes[argumentNumber]);
	}

	private int countNumberOfUnboundAnnotationArguments() {
		int count = 0;
		for (int i = 0; i < this.argumentTypes.length; i++) {
			if (isUnbound(i) && isSubtypeOf(Annotation.class, i)) {
				count++;
			}
		}
		return count;
	}

	private int countNumberOfUnboundPrimitiveArguments() {
		int count = 0;
		for (int i = 0; i < this.argumentTypes.length; i++) {
			if (isUnbound(i) && this.argumentTypes[i].isPrimitive()) {
				count++;
			}
		}
		return count;
	}

	/*
	 * Find the argument index with the given type, and bind the given
	 * {@code varName} in that position.
	 */
	private void findAndBind(Class<?> argumentType, String varName) {
		for (int i = 0; i < this.argumentTypes.length; i++) {
			if (isUnbound(i) && isSubtypeOf(argumentType, i)) {
				bindParameterName(i, varName);
				return;
			}
		}
		throw new IllegalStateException("Expected to find an unbound argument of type '" +
				argumentType.getName() + "'");
	}


	/**
	 * Simple struct to hold the extracted text from a pointcut body, together
	 * with the number of tokens consumed in extracting it.
	 */
	private static class PointcutBody {

		private int numTokensConsumed;

		@Nullable
		private String text;

		public PointcutBody(int tokens, @Nullable String text) {
			this.numTokensConsumed = tokens;
			this.text = text;
		}
	}


	/**
	 * Thrown in response to an ambiguous binding being detected when
	 * trying to resolve a method's parameter names.
	 */
	@SuppressWarnings("serial")
	public static class AmbiguousBindingException extends RuntimeException {

		/**
		 * Construct a new AmbiguousBindingException with the specified message.
		 * @param msg the detail message
		 */
		public AmbiguousBindingException(String msg) {
			super(msg);
		}
	}

}