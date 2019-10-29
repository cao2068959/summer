package chy.test;

import com.chy.summer.framework.boot.SummerApplication;
import com.chy.summer.framework.boot.autoconfigure.SummerBootApplication;
import com.chy.summer.framework.web.servlet.AnnotationConfigServletWebServerApplicationContext;

@SummerBootApplication
public class Main {

    public static void main(String[] args) {
        SummerApplication.run(Main.class,args);

    }


}
