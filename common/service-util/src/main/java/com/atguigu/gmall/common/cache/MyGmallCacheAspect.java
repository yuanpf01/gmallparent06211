package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author yuanpf
 * @create 2020-12-08 10:13
 */
@Aspect
@Component
public class MyGmallCacheAspect {
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @SneakyThrows
    @Around("@annotation(com.atguigu.gmall.common.cache.MyGmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint joinPoint){
        Object object = new Object();
        try {
            //找到注解,扫描有注解的类
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
             //获取到注解
            MyGmallCache annotation = signature.getMethod().getAnnotation(MyGmallCache.class);
            //获取注解中的前缀
            String prefix = annotation.prefix().toString();
            //获取方法中的参数
            Object[] args = joinPoint.getArgs();
            //定义数据在缓存中的key  注解中的【参数】前缀+ 方法的参数
            String dataKey = prefix + Arrays.asList(args).toString();
            //根据key在缓存中读取数据
            //object = redisTemplate.opsForValue().get(dataKey);
            object = cacheHit(dataKey,signature);

            if (object == null){//缓存中没有数据，采用分布式锁
                RLock lock = redissonClient.getLock(dataKey + ":lock");
                //上锁
                boolean flag = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX1, TimeUnit.SECONDS);
                if (flag){//上锁成功，从数据库查询数据
                    object = joinPoint.proceed(args);//执行方法体
                    if (object == null){//数据库中没有数据
                        Object object1 = new Object();
                        //放到缓存
                        redisTemplate.opsForValue().set(dataKey,
                                                        object1,
                                                        RedisConst.SKUKEY_TEMPORARY_TIMEOUT,
                                                        TimeUnit.SECONDS);
                        return object1;
                    }else {//数据库中有数据
                        redisTemplate.opsForValue().set(dataKey,
                                                        object,
                                                        RedisConst.SKUKEY_TIMEOUT,
                                                        TimeUnit.SECONDS);
                        return object;
                    }
                }else {//上锁失败
                    //等待
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //自旋
                    cacheAroundAdvice(joinPoint);
                }
            }else {
                //缓存中有数据
                return object;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return joinPoint.proceed(joinPoint.getArgs());//出现异常，使用数据库兜底
    }

    //将object类型的对象转换为查询得到的对应的类型
    private Object cacheHit(String dataKey, MethodSignature signature) {
        //从缓存中获取数据
        String object = (String)redisTemplate.opsForValue().get(dataKey);
        //String object = (String) redisTemplate.opsForValue().get(dataKey);
        if (!StringUtils.isEmpty(object)){//数据非空，进行类型转换
            //获取方法中返回值的类型
            Class returnType = signature.getReturnType();
            return JSON.parseObject(object,returnType);
        }
        return null;
    }
}
