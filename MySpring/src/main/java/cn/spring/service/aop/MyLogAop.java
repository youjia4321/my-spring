package cn.spring.service.aop;

import org.springfremaker.bean.annotation.Around;
import org.springfremaker.bean.annotation.Aspect;
import org.springfremaker.bean.aop.ProceedingJoinPoint;

@Aspect
public class MyLogAop {
    @Around(value = "cn.spring.service.impl.UserServiceImpl.zhuanZhang")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
        System.out.println("在方法运行前。。。进行日志记录");
        //目标方法
        Object proceed = proceedingJoinPoint.proceed();
        System.out.println("在方法运行后。。。进行日志记录");
        return proceed;
    }
}
