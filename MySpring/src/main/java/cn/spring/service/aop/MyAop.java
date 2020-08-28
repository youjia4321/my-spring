package cn.spring.service.aop;

import org.springfremaker.bean.annotation.Around;
import org.springfremaker.bean.aop.ProceedingJoinPoint;


//@Aspect
public class MyAop {

    @Around(value = "cn.spring.service.impl.UserServiceImpl.shoot")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
        System.out.println("前置通知");
        Object proceed = proceedingJoinPoint.proceed();
        System.out.println("后置通知");
        return proceed;
    }

}
