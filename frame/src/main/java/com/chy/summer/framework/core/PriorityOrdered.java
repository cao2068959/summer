package com.chy.summer.framework.core;

/**
 * 扩展了Ordered接口，表示优先顺序,PriorityOrdered对象始终在普通Ordered对象之前应用，而不管其顺序值如何。
 * 是一个专用接口，这主要在框架本身内用于对象
 */
public interface PriorityOrdered extends Ordered {

}