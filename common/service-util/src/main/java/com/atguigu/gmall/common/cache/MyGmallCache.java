package com.atguigu.gmall.common.cache;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author yuanpf
 * @create 2020-12-08 10:05
 */
@Target(ElementType.METHOD) //注解可以用在哪些位置：方法体、类、、、、、
@Retention(RetentionPolicy.RUNTIME) //生命周期
@Documented  //文档注释
public @interface MyGmallCache {

    String prefix() default "";//前缀
}
