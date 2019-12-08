package com.chy.summer.framework.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class AutowireUtils {


    /**
     * 工厂方法排序 排序顺序是 public优先  参数多的优先
     * @param factoryMethods
     */
    public static void sortFactoryMethods(Executable[] factoryMethods) {
        Arrays.sort(factoryMethods, (fm1, fm2) -> {
            boolean p1 = Modifier.isPublic(fm1.getModifiers());
            boolean p2 = Modifier.isPublic(fm2.getModifiers());
            if (p1 != p2) {
                return (p1 ? -1 : 1);
            }
            int c1pl = fm1.getParameterCount();
            int c2pl = fm2.getParameterCount();
            return (c1pl < c2pl ? 1 : (c1pl > c2pl ? -1 : 0));
        });
    }

}
