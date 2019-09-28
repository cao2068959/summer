package chy.test;

import com.chy.summer.framework.web.servlet.AnnotationConfigServletWebServerApplicationContext;

public class Main {

    public static void main(String[] args) {
        AnnotationConfigServletWebServerApplicationContext annotationConfigServletWebServerApplicationContext =
                new AnnotationConfigServletWebServerApplicationContext("classpath*:chy/test");


    }


}
