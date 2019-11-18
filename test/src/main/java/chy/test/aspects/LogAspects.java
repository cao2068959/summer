package chy.test.aspects;

import chy.test.annotation.ChyService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
@ChyService
public class LogAspects {
    @Pointcut("execution(* chy.test.service.*.*(..))")
    public void pointCut() { }

    @Before("pointCut()")
    public void logStart(JoinPoint joinPoint) {
        System.out.println("前置通知");
    }
}