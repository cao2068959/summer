package com.chy.summer.framework.core.annotation;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.Function;

/**
 * 注解属性的 持有类，他维护了 整个注解的继承关系，一个 holder 对应一个 AnnotationAttribute
 * 也可以认为 一个 holder 就是一个 annotation 解析成一个java对象后的形态
 * A      @C
 * / \     @B
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

    @Getter
    private Map<String, AnnotationAttributeHolder> child = new HashMap<>();

    @Getter
    private List<AnnotationAlias> annotationAliasList ;

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
        contain.add(annotationAttributeHolder.getName());
        annotationAttributeHolder.setParent(this);

        //给所有的父节点 把新加入的 子节点的 name 给加上去
        forParent((parent) -> {
            parent.contain.add(annotationAttributeHolder.getName());
            return null;
        }, null);

    }

    /**
     * 通过父节点去检查 有没循环引用
     *
     * @return
     */
    public Boolean closeLoop(String name) {
        return forParent((parent) -> {
            if (parent.getName().equals(name)) {
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

    /**
     * 判断是否有 @Alias 注解的属性
     *
     * @return
     */
    public boolean hasAlias() {
        if (annotationAliasList == null || annotationAliasList.size() == 0) {
            return false;
        }
        return true;
    }

    /**
     * 判定 这个注解里有没有包含了直接注解,这里的判定包括了 派生注解
     *
     * @param annotationName
     * @return
     */
    public boolean hashAnnotation(String annotationName) {
        if (name.equals(annotationName)) {
            return true;
        }
        return contain.contains(annotationName);
    }

    /**
     * 获取这个注解下面的子注解,可以根据子注解里包含的孙注解来查找
     *
     * 子注解  @A @B @C  其中 @A @C 继承了 注解 @T 那么  入参(@T的Name) -> 将得到   [@A,@C]
     * 同时也能  入参(@A的Name) -> 将得到   [@A]
     *
     *
     * @param annotationName
     * @return
     */
    public List<AnnotationAttributeHolder> getChildAnntationHolder(String annotationName){
        List result = new LinkedList();
        //如果 所有的子注解都没包含 目标注解
        if(!contain.contains(annotationName)){
            return result;
        }

        AnnotationAttributeHolder childHolder = child.get(annotationName);
        if(childHolder != null){
            result.add(childHolder);
        }
        child.values().stream().forEach(holder -> {
            if(holder.getContain().contains(annotationName)){
                result.add(holder);
            }
        });
        return result;
    }

    /**
     * 如果  TargerName 和 TargerClass 和某个任务相同 就会去移除对应的任务
     * @param annotationAlias
     */
    public void removeAlias(AnnotationAlias annotationAlias){
        if(annotationAliasList == null ){
            return;
        }
        if(!(contain.contains(annotationAlias.getTargerClass()))){
            return;
        }

        annotationAliasList.removeIf(alias->{
            if(alias.getTargerName().equals(alias.getTargerName()) && alias.getTargerClass().equals(alias.getTargerClass())){
                return true;
            }
            return false;
        });
    }


}
