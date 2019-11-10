package chy.test;

import chy.test.service.Abc;
import com.chy.summer.framework.boot.SummerApplication;
import com.chy.summer.framework.boot.autoconfigure.SummerBootApplication;
import com.chy.summer.framework.context.ApplicationContext;

@SummerBootApplication
public class Main {

    public static void main(String[] args) {

        ApplicationContext context = SummerApplication.run(Main.class, args);
        Abc abc = context.getBean("啦啦啦啦",Abc.class);
        abc.test();

        abc.abc2.test2();
    }

}
