package chy.test;

import chy.test.service.Abc;
import chy.test.service.BeanDemo2;
import com.chy.summer.framework.boot.SummerApplication;
import com.chy.summer.framework.boot.autoconfigure.SummerBootApplication;
import com.chy.summer.framework.context.ApplicationContext;
import com.chy.summer.framework.context.annotation.EnableAspectJAutoProxy;

@SummerBootApplication
@EnableAspectJAutoProxy
public class Main {

    public static void main(String[] args) {

        ApplicationContext context = SummerApplication.run(Main.class, args);
        BeanDemo2 abc = context.getBean("beanDemo33", BeanDemo2.class);
        System.out.println(abc.getName());

    }

}
