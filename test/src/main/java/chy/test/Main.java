package chy.test;

import chy.test.service.Abc;
import com.chy.summer.framework.boot.SummerApplication;
import com.chy.summer.framework.boot.autoconfigure.SummerBootApplication;
import com.chy.summer.framework.context.ApplicationContext;

@SummerBootApplication
public class Main {

    public static void main(String[] args) {

        ApplicationContext context = SummerApplication.run(Main.class, args);
        Abc abc2 = context.getBean("啦啦啦啦",Abc.class);
        abc2.test();
    }


}
