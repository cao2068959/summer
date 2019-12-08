package com.chy.summer.framework.beans.factory;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 构造器的参数类
 */
public class ConstructorArgumentValues {

    private final Map<Integer, ValueHolder> indexedArgumentValues = new LinkedHashMap<Integer, ValueHolder>(0);

    private final List<ValueHolder> genericArgumentValues = new LinkedList<ValueHolder>();

    public ConstructorArgumentValues(ConstructorArgumentValues original) {
        addArgumentValues(original);
    }

    public ConstructorArgumentValues() {
    }


    public void addArgumentValues(ConstructorArgumentValues other) {
        if (other != null) {
            //TODO 真正的逻辑
        }
    }


    /**
     * 判断是否有构造器参数
     * @return
     */
    public boolean isEmpty() {
        return (this.indexedArgumentValues.isEmpty() && this.genericArgumentValues.isEmpty());
    }

    /**
     * 获取构造参数的数量
     * @return
     */
    public int getArgumentCount() {
        return (this.indexedArgumentValues.size() + this.genericArgumentValues.size());
    }

    public static class ValueHolder {

        @Getter
        @Setter
        private Object value;

        @Getter
        @Setter
        private String type;

        @Getter
        @Setter
        private String name;

        @Getter
        @Setter
        private Object source;


        private boolean converted = false;

        private Object convertedValue;


        public ValueHolder(Object value) {
            this.value = value;
        }


        public ValueHolder(Object value, String type) {
            this.value = value;
            this.type = type;
        }


        public ValueHolder(Object value, String type, String name) {
            this.value = value;
            this.type = type;
            this.name = name;
        }

        public synchronized boolean isConverted() {
            return this.converted;
        }

        public synchronized void setConvertedValue(Object value) {
            this.converted = true;
            this.convertedValue = value;
        }


        public synchronized Object getConvertedValue() {
            return this.convertedValue;
        }


        public ValueHolder copy() {
            ValueHolder copy = new ValueHolder(this.value, this.type, this.name);
            copy.setSource(this.source);
            return copy;
        }
    }

}
