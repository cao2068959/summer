package com.chy.summer.framework.boot.origin;


import lombok.Getter;

/**
 * 用来存放配置文件的 value值
 */
public class OriginTrackedValue implements OriginProvider {


    @Getter
    private final Object value;

    @Getter
    private final Origin origin;

    private OriginTrackedValue(Object value, Origin origin) {
        this.value = value;
        this.origin = origin;
    }

    public static OriginTrackedValue of(Object value, Origin origin) {
        if (value == null) {
            return null;
        }
        if (value instanceof CharSequence) {
            return new OriginTrackedCharSequence((CharSequence) value, origin);
        }
        return new OriginTrackedValue(value, origin);
    }

    private static class OriginTrackedCharSequence extends OriginTrackedValue implements CharSequence {

        OriginTrackedCharSequence(CharSequence value, Origin origin) {
            super(value, origin);
        }

        @Override
        public int length() {
            return getValue().length();
        }

        @Override
        public char charAt(int index) {
            return getValue().charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return getValue().subSequence(start, end);
        }

        @Override
        public CharSequence getValue() {
            return (CharSequence) super.getValue();
        }

    }

}
