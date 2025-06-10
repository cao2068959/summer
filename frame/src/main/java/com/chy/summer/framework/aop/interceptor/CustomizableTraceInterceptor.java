package com.chy.summer.framework.aop.interceptor;

import com.chy.summer.framework.aop.aopalliance.intercept.MethodInvocation;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.StringUtils;
import com.chy.summer.framework.util.core.StopWatch;
import javax.annotation.Nullable;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 允许使用占位符进行可高度定制的方法级跟踪的MethodInterceptor实现
 */
public class CustomizableTraceInterceptor extends AbstractTraceInterceptor {

    /**
     * $[methodName]占位符
     * 替换为要调用的方法的名称。
     */
    public static final String PLACEHOLDER_METHOD_NAME = "$[methodName]";

    /**
     * $[targetClassName]占位符。
     * 替换为方法调用目标类的完全限定名
     */
    public static final String PLACEHOLDER_TARGET_CLASS_NAME = "$[targetClassName]";

    /**
     * $[targetClassShortName]占位符。
     * 用方法调用目标的Class的简称替换
     */
    public static final String PLACEHOLDER_TARGET_CLASS_SHORT_NAME = "$[targetClassShortName]";

    /**
     * $[returnValue]占位符。
     * 替换为方法调用返回的值的String表示形式。
     */
    public static final String PLACEHOLDER_RETURN_VALUE = "$[returnValue]";

    /**
     * $[argumentTypes]占位符。
     * 替换为方法调用的参数类型的逗号分隔列表。 参数类型被写为简短的类名。
     */
    public static final String PLACEHOLDER_ARGUMENT_TYPES = "$[argumentTypes]";

    /**
     * $[arguments]占位符。
     * 用逗号分隔的方法调用参数值列表替换。 依赖于每种参数类型的toString()方法。
     */
    public static final String PLACEHOLDER_ARGUMENTS = "$[arguments]";

    /**
     * $[exception]占位符。
     * 用方法调用期间引发的任何Throwable的String表示形式替换。
     */
    public static final String PLACEHOLDER_EXCEPTION = "$[exception]";

    /**
     * $[invocationTime]占位符
     * 替换为调用所花费的时间（以毫秒为单位）
     */
    public static final String PLACEHOLDER_INVOCATION_TIME = "$[invocationTime]";

    private static final Set<Object> PLACEHOLDERS = new HashSet<>();

    static {
        PLACEHOLDERS.add(PLACEHOLDER_METHOD_NAME);
        PLACEHOLDERS.add(PLACEHOLDER_TARGET_CLASS_NAME);
        PLACEHOLDERS.add(PLACEHOLDER_TARGET_CLASS_SHORT_NAME);
        PLACEHOLDERS.add(PLACEHOLDER_RETURN_VALUE);
        PLACEHOLDERS.add(PLACEHOLDER_ARGUMENT_TYPES);
        PLACEHOLDERS.add(PLACEHOLDER_ARGUMENTS);
        PLACEHOLDERS.add(PLACEHOLDER_EXCEPTION);
        PLACEHOLDERS.add(PLACEHOLDER_INVOCATION_TIME);
    }

    /**
     * 用于编写方法调用之前消息的默认消息
     */
    private static final String DEFAULT_ENTER_MESSAGE = "入口方式[" + PLACEHOLDER_TARGET_CLASS_NAME + "]的方法'" +
            PLACEHOLDER_METHOD_NAME + "'";

    /**
     * 用于编写方法结束之后消息的默认消息
     */
    private static final String DEFAULT_EXIT_MESSAGE = "退出方式[" + PLACEHOLDER_TARGET_CLASS_NAME + "]的方法'" +
            PLACEHOLDER_METHOD_NAME + "'";

    /**
     * 用于编写异常消息的默认消息
     */
    private static final String DEFAULT_EXCEPTION_MESSAGE = "抛出异常[" + PLACEHOLDER_TARGET_CLASS_NAME + "]的方法'" +
            PLACEHOLDER_METHOD_NAME + "'";

    /**
     * 用于匹配占位符的模式
     */
    private static final Pattern PATTERN = Pattern.compile("\\$\\[\\p{Alpha}+\\]");

    /**
     * 允许的占位符合集
     */
    private static final Set<Object> ALLOWED_PLACEHOLDERS = PLACEHOLDERS;


    /**
     * 方法执行前消息
     */
    private String enterMessage = DEFAULT_ENTER_MESSAGE;

    /**
     * 方法结束后的消息
     */
    private String exitMessage = DEFAULT_EXIT_MESSAGE;

    /**
     * 方法执行期间的异常消息
     */
    private String exceptionMessage = DEFAULT_EXCEPTION_MESSAGE;


    /**
     * 设置用于方法输入日志消息的模板。
     * 该模板可以包含以下任何占位符：
     * $[targetClassName]
     * $[targetClassShortName]
     * $[argumentTypes]
     * $[arguments]
     */
    public void setEnterMessage(String enterMessage) throws IllegalArgumentException {
        Assert.hasText(enterMessage, "enterMessage不可为空");
        checkForInvalidPlaceholders(enterMessage);
        Assert.doesNotContain(enterMessage, PLACEHOLDER_RETURN_VALUE,
                "enterMessage不能包含占位符" + PLACEHOLDER_RETURN_VALUE);
        Assert.doesNotContain(enterMessage, PLACEHOLDER_EXCEPTION,
                "enterMessage不能包含占位符" + PLACEHOLDER_EXCEPTION);
        Assert.doesNotContain(enterMessage, PLACEHOLDER_INVOCATION_TIME,
                "enterMessage不能包含占位符" + PLACEHOLDER_INVOCATION_TIME);
        this.enterMessage = enterMessage;
    }

    /**
     * 设置用于方法结束日志消息的模板。
     * 该模板可以包含以下任何占位符：
     * $[targetClassName]
     * $[targetClassShortName]
     * $[argumentTypes]
     * $[arguments]
     * $[returnValue]
     * $[invocationTime]
     */
    public void setExitMessage(String exitMessage) {
        Assert.hasText(exitMessage, "exitMessage不可为空");
        checkForInvalidPlaceholders(exitMessage);
        Assert.doesNotContain(exitMessage, PLACEHOLDER_EXCEPTION,
                "exitMessage不能包含占位符" + PLACEHOLDER_EXCEPTION);
        this.exitMessage = exitMessage;
    }

    /**
     * 设置用于方法异常日志消息的模板
     * 该模板可以包含以下任何占位符：
     * $[targetClassName]
     * $[targetClassShortName]
     * $[argumentTypes]
     * $[arguments]
     * $[exception]
     */
    public void setExceptionMessage(String exceptionMessage) {
        Assert.hasText(exceptionMessage, "exceptionMessage不可为空");
        checkForInvalidPlaceholders(exceptionMessage);
        Assert.doesNotContain(exceptionMessage, PLACEHOLDER_RETURN_VALUE,
                "exceptionMessage不能包含占位符" + PLACEHOLDER_RETURN_VALUE);
        this.exceptionMessage = exceptionMessage;
    }


    /**
     * 根据enterMessage的值在调用之前写入日志消息
     * 如果调用成功，则会方法结束处基于值exitMessage写入一条日志消息。 如果在调用期间发生异常，则根据exceptionMessage的值编写一条消息
     */
    @Override
    protected Object invokeUnderTrace(MethodInvocation invocation, Logger logger) throws Throwable {
        //获取调用方法的方法名
        String name = ClassUtils.getQualifiedMethodName(invocation.getMethod());
        //创建一个计时器
        StopWatch stopWatch = new StopWatch(name);
        Object returnValue = null;
        boolean exitThroughException = false;
        try {
            //启动计时器
            stopWatch.start(name);
            //编写执行前的日志
            writeToLog(logger,
                    replacePlaceholders(this.enterMessage, invocation, null, null, -1));
            //执行方法
            returnValue = invocation.proceed();
            return returnValue;
        } catch (Throwable ex) {
            if (stopWatch.isRunning()) {
                //停止计时器
                stopWatch.stop();
            }
            exitThroughException = true;
            //编写方法异常的日志
            writeToLog(logger, replacePlaceholders(
                    this.exceptionMessage, invocation, null, ex, stopWatch.getTotalTimeMillis()), ex);
            throw ex;
        } finally {
            if (!exitThroughException) {
                if (stopWatch.isRunning()) {
                    //停止计时器
                    stopWatch.stop();
                }
                //编写方法调用后的日志
                writeToLog(logger, replacePlaceholders(
                        this.exitMessage, invocation, returnValue, null, stopWatch.getTotalTimeMillis()));
            }
        }
    }

    /**
     * 编写日志
     * 用提供的值或从提供的值的派生值替换给定消息中的占位符
     *
     * @param message          包含要替换的占位符的消息模板
     * @param methodInvocation 正在记录的MethodInvocation。 用于导出除$[exception]和$[returnValue]以外的所有占位符的值
     * @param returnValue      调用返回的任何值。 用于替换$[returnValue]占位符。 可能为空。
     * @param throwable        调用期间引发的任何Throwable。 Throwable.toString()的值替换为$[exception]占位符。 可能为空。
     * @param invocationTime   代替$[invocationTime]占位符写入的值
     * @return 格式化后的输出日志
     */
    protected String replacePlaceholders(String message, MethodInvocation methodInvocation,
                                         @Nullable Object returnValue, @Nullable Throwable throwable, long invocationTime) {

        Matcher matcher = PATTERN.matcher(message);

        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            String match = matcher.group();
            if (PLACEHOLDER_METHOD_NAME.equals(match)) {
                matcher.appendReplacement(output, Matcher.quoteReplacement(methodInvocation.getMethod().getName()));
            } else if (PLACEHOLDER_TARGET_CLASS_NAME.equals(match)) {
                String className = getClassForLogging(methodInvocation.getThis()).getName();
                matcher.appendReplacement(output, Matcher.quoteReplacement(className));
            } else if (PLACEHOLDER_TARGET_CLASS_SHORT_NAME.equals(match)) {
                String shortName = ClassUtils.getShortName(getClassForLogging(methodInvocation.getThis()));
                matcher.appendReplacement(output, Matcher.quoteReplacement(shortName));
            } else if (PLACEHOLDER_ARGUMENTS.equals(match)) {
                matcher.appendReplacement(output,
                        Matcher.quoteReplacement(StringUtils.arrayToCommaDelimitedString(methodInvocation.getArguments())));
            } else if (PLACEHOLDER_ARGUMENT_TYPES.equals(match)) {
                appendArgumentTypes(methodInvocation, matcher, output);
            } else if (PLACEHOLDER_RETURN_VALUE.equals(match)) {
                appendReturnValue(methodInvocation, matcher, output, returnValue);
            } else if (throwable != null && PLACEHOLDER_EXCEPTION.equals(match)) {
                matcher.appendReplacement(output, Matcher.quoteReplacement(throwable.toString()));
            } else if (PLACEHOLDER_INVOCATION_TIME.equals(match)) {
                matcher.appendReplacement(output, Long.toString(invocationTime));
            } else {
                // Should not happen since placeholders are checked earlier.
                throw new IllegalArgumentException("未知的占位符[" + match + "]");
            }
        }
        matcher.appendTail(output);

        return output.toString();
    }

    /**
     * 将方法返回值的String表示形式添加到提供的StringBuffer中。 正确处理无效结果
     *
     * @param methodInvocation 返回值的MethodInvocation
     * @param matcher          包含匹配的占位符的Matcher
     * @param output           将StringBuffer写入输出
     * @param returnValue      方法调用返回的值
     */
    private void appendReturnValue(
            MethodInvocation methodInvocation, Matcher matcher, StringBuffer output, @Nullable Object returnValue) {

        if (methodInvocation.getMethod().getReturnType() == void.class) {
            matcher.appendReplacement(output, "void");
        } else if (returnValue == null) {
            matcher.appendReplacement(output, "null");
        } else {
            matcher.appendReplacement(output, Matcher.quoteReplacement(returnValue.toString()));
        }
    }

    /**
     * 将方法参数类型的短类名的逗号分隔列表添加到输出中
     * 例如，如果一个方法具有签名put（java.lang.String，java.lang.Object），则返回的值将是String，Object。
     *
     * @param methodInvocation 正在记录的MethodInvocation。 参数将从相应的方法中检索。
     * @param matcher          包含输出的Matcher
     * @param output           包含输出的StringBuffer
     */
    private void appendArgumentTypes(MethodInvocation methodInvocation, Matcher matcher, StringBuffer output) {
        //获取方法的参数值类型
        Class<?>[] argumentTypes = methodInvocation.getMethod().getParameterTypes();
        String[] argumentTypeShortNames = new String[argumentTypes.length];
        for (int i = 0; i < argumentTypeShortNames.length; i++) {
            argumentTypeShortNames[i] = ClassUtils.getShortName(argumentTypes[i]);
        }
        matcher.appendReplacement(output,
                Matcher.quoteReplacement(StringUtils.arrayToCommaDelimitedString(argumentTypeShortNames)));
    }

    /**
     * 检查所提供的字符串是否有未在该类上指定为常量的占位符，如果有，则抛出IllegalArgumentException
     */
    private void checkForInvalidPlaceholders(String message) throws IllegalArgumentException {
        Matcher matcher = PATTERN.matcher(message);
        while (matcher.find()) {
            String match = matcher.group();
            if (!ALLOWED_PLACEHOLDERS.contains(match)) {
                throw new IllegalArgumentException("占位符[" + match + "]无效");
            }
        }
    }

}