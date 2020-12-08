package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.impl.TestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yuanpf
 * @create 2020-12-07 18:25
 */
@RestController
@RequestMapping("admin/product/test")
public class TestController {

    @Autowired
    TestServiceImpl testService;

    @GetMapping("testLock")
    public Result testLock(){

        testService.testLockService();
        return Result.ok();
    }
    /**
     * 测试读写锁
     *
     */
    //读锁
    @GetMapping("read")
    public Result readLockTest(){
        String msg = testService.readLock();
        return Result.ok(msg);
    }
    //写锁
    @GetMapping("write")
    public Result writeLockTest(){
        String msg = testService.writeLock();
        return Result.ok(msg);
    }

}
