package chy.test;

import chy.test.service.Abc;
import chy.test.service.Abc2;
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
        context.getBean("abc2222222", Abc2.class);
        Abc2 abc = context.getBean("abc2222222", Abc2.class);
        abc.test2();
        BeanDemo2 beanDemo33 = context.getBean("beanDemo33", BeanDemo2.class);
        System.out.println(beanDemo33.getName());

    }

}
