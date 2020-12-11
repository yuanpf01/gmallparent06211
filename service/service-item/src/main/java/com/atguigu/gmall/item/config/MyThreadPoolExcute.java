package com.atguigu.gmall.item.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yuanpf
 * @create 2020-12-08 16:44
 */
@Configuration
public class MyThreadPoolExcute {
    /*@Bean
    public ThreadPoolExecutor getThreadPoolExecutor(){
        ThreadPoolExecutor myThreadPoolExecutor =
                new ThreadPoolExecutor(5,
                                  20,
                                     3L,
                                                   TimeUnit.SECONDS,
                                                   new ArrayBlockingQueue<>(5));//拒绝策略和工厂可以采用默认的，也可以自己声明
        return myThreadPoolExecutor;
    }
}
*/


    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        //  创建线程池
        return new ThreadPoolExecutor(
                3,
                5,
                3l,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100)
        );
    }
}
