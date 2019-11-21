package chy.test.aspects;

import chy.test.annotation.ChyService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
@ChyService
public class LogAspects {
    @Pointcut("execution(* chy.test.service.Abc.*(..))")
    public void pointCut() { }

    @Before("pointCut()")
    public void logStart(JoinPoint joinPoint) {
        System.out.println("前置通知");
    }
    @After("pointCut()")
    public void afterMethod(JoinPoint joinPoint){
        System.out.println("后置通知");
    }
    @AfterReturning("pointCut()")
    public void afterReturning(JoinPoint joinPoint){
        System.out.println("返回通知");
    }
}