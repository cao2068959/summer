package com.chy.summer.framework.core.annotation;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.Function;

/**
 * 注解属性的 持有类，他维护了 整个注解的继承关系，一个 holder 对应一个 AnnotationAttribute
 * 也可以认为 一个 holder 就是一个 annotation 解析成一个java对象后的形态
 *   A      @C
 *  / \     @B
 * B   C    public @interface A{}
 */
public class AnnotationAttributeHolder {

    /**
     * 当前注解的名称
     */
    @Getter
    private String name;

    /**
     * 对应注解的属性
     */
    @Getter
    private AnnotationAttributes annotationAttributes;

    /**
     * 父节点
     */
    @Getter
    @Setter
    private AnnotationAttributeHolder parent;

    /**
     * 记录了这个注解下面 所有的子类注解(派生、继承)，不包括 java 的原生注解
     */
    @Getter
    private Set<String> contain = new HashSet<>();

    private Map<String, AnnotationAttributeHolder> child = new HashMap<>();

    private List<AnnotationAlias> annotationAliasList;

    public AnnotationAttributeHolder(String name, AnnotationAttributes annotationAttributes) {
        this.name = name;
        this.annotationAttributes = annotationAttributes;
    }

    public void addAlias(AnnotationAlias annotationAlias) {
        if (annotationAliasList == null) {
            annotationAliasList = new LinkedList<>();
        }
        annotationAliasList.add(annotationAlias);
    }


    public void addChild(AnnotationAttributeHolder annotationAttributeHolder) {
        child.put(annotationAttributeHolder.getName(), annotationAttributeHolder);
        annotationAttributeHolder.setParent(this);

        //给所有的父节点 把新加入的 子节点的 name 给加上去
        forParent((parent)->{
            contain.add(annotationAttributeHolder.getName());
            return null;
        },null);

    }

    /**
     * 通过父节点去检查 有没循环引用
     *
     * @return
     */
    public Boolean closeLoop(String name) {
        return forParent((parent) -> {
            if(parent.getName().equals(name)){
                return true;
            }
            return null;
        }, false);
    }

    /**
     * 逆序循环了 所有的父节点
     */
    private <T> T forParent(Function<AnnotationAttributeHolder, T> function, T defaultResult) {
        AnnotationAttributeHolder point = this;
        do {
            T result = function.apply(point);
            if (result != null) {
                return result;
            }
            point = point.getParent();
        } while (point != null);

        return defaultResult;
    }


}
