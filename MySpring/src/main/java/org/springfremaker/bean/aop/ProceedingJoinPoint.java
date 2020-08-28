package org.springfremaker.bean.aop;


import java.lang.reflect.Method;

/**
 * method.invoke(对象，参数)
 */

public class ProceedingJoinPoint {
    //因为待会需要调用目标方法 这个就是目标方法
    private Method method;
    //方法参数
    private Object[] args;
    //对象
    private Object obj;

   public  Object proceed() throws Throwable{
        return method.invoke(obj, args);
    }

    public ProceedingJoinPoint(Method method, Object[] args, Object obj) {
        this.method = method;
        this.args = args;
        this.obj = obj;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
