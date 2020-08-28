package org.springfremaker.bean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//这个注解可以作用在运行期
@Retention(RetentionPolicy.RUNTIME)
//指定该注解可以作用在类上
@Target(ElementType.TYPE)
public @interface Service {
    String value() default "";
}
