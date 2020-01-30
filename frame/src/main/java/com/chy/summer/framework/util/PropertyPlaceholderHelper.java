package com.chy.summer.framework.util;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 用了解析占位符的工具类
 */
@Slf4j
public class PropertyPlaceholderHelper {

    /**
     * 要匹配的前缀 如: ${
     */
    private final String placeholderPrefix;

    /**
     * 要匹配的后缀 如: }
     */
    private final String placeholderSuffix;

    private final boolean ignoreUnresolvablePlaceholders;

    /**
     * 对比的值
     */
    private final String valueSeparator;

    /**
     * 用来解析括号匹配的时候的判定前缀: 如果之前的 前缀是 ${ 那么 这个值就是 {,会根据 wellKnownSimplePrefixes 里的值剪裁
     * 原因: 比如 ${123{abc}} 如果还是使用 ${ 作为前缀去匹配那么 最终匹配到的 位置将会 9(正确应该是10)
     */
    private final String simplePrefix;

    private static final Map<String, String> wellKnownSimplePrefixes = new HashMap<>(4);

    static {
        wellKnownSimplePrefixes.put("}", "{");
        wellKnownSimplePrefixes.put("]", "[");
        wellKnownSimplePrefixes.put(")", "(");
    }

    public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix,
                                     String valueSeparator, boolean ignoreUnresolvablePlaceholders) {
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
        this.valueSeparator = valueSeparator;
        this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
        String simplePrefixForSuffix = wellKnownSimplePrefixes.get(this.placeholderSuffix);
        if (simplePrefixForSuffix != null && this.placeholderPrefix.endsWith(simplePrefixForSuffix)) {
            this.simplePrefix = simplePrefixForSuffix;
        } else {
            this.simplePrefix = this.placeholderPrefix;
        }
    }

    public String replacePlaceholders(String value, PlaceholderResolver placeholderResolver) {
        Assert.notNull(value, "'value' must not be null");
        return parseStringValue(value, placeholderResolver, null);
    }

    /**
     * 用于解析 占位符的方法
     */
    protected String parseStringValue(
            String value, PlaceholderResolver placeholderResolver, Set<String> visitedPlaceholders) {

        int startIndex = value.indexOf(this.placeholderPrefix);
        //如果传入的 value 不满足占位符的 前缀，那么说明没有占位符，直接返回
        if (startIndex == -1) {
            return value;
        }


        StringBuffer result = new StringBuffer(value);
        while (startIndex != -1) {
            //知道了 前缀的开始位置,计算出对应后缀的 index
            int endIndex = findPlaceholderEndIndex(result, startIndex);
            //如果 没找到结束标志,说明并没有一个完整的表达式,就直接返回结果了
            if (endIndex == -1) {
                return result.toString();
            }

            //把表达式给解析出来
            String placeholder = result.substring(startIndex + this.placeholderPrefix.length(), endIndex);

            String originalPlaceholder = placeholder;
            //visitedPlaceholders 这个属性是递归的时候 带上每次处理过的 str都存一遍,能防止循环递归
            if (visitedPlaceholders == null) {
                visitedPlaceholders = new HashSet<>(4);
            }
            //添加失败了,说明容器里面有相同的 str 可能发生了循环调用,这里就直接抛出异常了
            if (!visitedPlaceholders.add(originalPlaceholder)) {
                throw new IllegalArgumentException(
                        "Circular placeholder reference '" + originalPlaceholder + "' in property definitions");
            }

            //递归去处理解析出来的 表达式 因为: ${aaa${bbb}} -> ${bbb} -> bbb 需要递归直到解析到 bbb
            placeholder = parseStringValue(placeholder, placeholderResolver, visitedPlaceholders);

            //拿到了表达式后,去调用正真解析表达式的方法来获取表达式的值
            String propVal = placeholderResolver.resolvePlaceholder(placeholder);

            //如果解析出来的值是Null,并且传入了分隔符,那么就取默认值
            //比如 ${aaa:1234} -> 解析 aaa:1234 -> 解析值 aaa==null -> 取 1234 作为默认值
            if (propVal == null && this.valueSeparator != null) {
                int separatorIndex = placeholder.indexOf(this.valueSeparator);
                //计算在 表达式中有么有分隔符,没有就直接跳过下面的操作
                if (separatorIndex != -1) {
                    //  a:b -> 拿到 a
                    String actualPlaceholder = placeholder.substring(0, separatorIndex);
                    // a:b -> 拿到 b当做默认值
                    String defaultValue = placeholder.substring(separatorIndex + this.valueSeparator.length());
                    // a:b -> 重新用 a 去解析表达式
                    propVal = placeholderResolver.resolvePlaceholder(actualPlaceholder);
                    //如果还是等于 null, 那么 就直接赋予默认值了
                    if (propVal == null) {
                        propVal = defaultValue;
                    }

                }
            }

            //如果 解析出来了 对应的值
            if (propVal != null) {
                //表达式解析出来的值可能还带了,表达式 所以这里要再次递归处理
                propVal = parseStringValue(propVal, placeholderResolver, visitedPlaceholders);
                //把解析出来的 表达式的值给替换了  ${abc:123}-${wer:456} -> 123-${wer:456}
                result.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
                log.trace("解析出 表达式值 [{}]",result);
                startIndex = result.indexOf(this.placeholderPrefix, startIndex + propVal.length());
            }
            //如果 设置了 ignoreUnresolvablePlaceholders = true 那么就算表达式没有解析出值也继续解析
            else if (this.ignoreUnresolvablePlaceholders) {
                startIndex = result.indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length());
            }
            //表达式没有解析出值,直接报错了.
            else {
                throw new IllegalArgumentException("Could not resolve placeholder '" +
                        placeholder + "'" + " in value \"" + value + "\"");
            }
        }

        return result.toString();
    }


    /**
     * 给定 开始占位符的位置,计算出对应的 结束占位符的位置
     * ${123}-${abc} 如果传 0 则计算出 5, 如果传入 7 则计算出 12
     * ${123${abc}} 如果传0 则计算出 11
     *
     * @param value
     * @param startIndex
     * @return
     */
    private int findPlaceholderEndIndex(CharSequence value, Integer startIndex) {
        int index = startIndex + this.placeholderPrefix.length();
        int withinNestedPlaceholder = 0;
        while (index < value.length()) {
            if (StringUtils.substringMatch(value, index, this.placeholderSuffix)) {
                if (withinNestedPlaceholder > 0) {
                    withinNestedPlaceholder--;
                    index = index + this.placeholderSuffix.length();
                } else {
                    return index;
                }
            } else if (StringUtils.substringMatch(value, index, this.simplePrefix)) {
                withinNestedPlaceholder++;
                index = index + this.simplePrefix.length();
            } else {
                index++;
            }
        }
        return -1;
    }


    public static void main(String[] args) {
        PropertyPlaceholderHelper p = new PropertyPlaceholderHelper("${", "}", ":", false);
        System.out.println(p.parseStringValue("${aadbc}", (str)->{
            if("aadbc".equals(str)){
                return "${123332432}";
            }

            return str;

        },null));
    }


    public interface PlaceholderResolver {

        String resolvePlaceholder(String placeholderName);
    }

}
