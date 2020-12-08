package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.config.RedissonConfig;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.DuplicateFormatFlagsException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author yuanpf
 * @create 2020-12-07 18:26
 * 使用分布式锁，必须setex + setnx    set key value px[毫秒]或ex【秒】 过期时间
 * set age 20 ex 10
 *
 */
@Service
public class TestServiceImpl {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    public void testLockService(){
        RLock lock = null;
        try {
            //上锁
            lock = redissonClient.getLock("lock");
            lock.lock();

            //获取缓存存储的数据
            String num = redisTemplate.opsForValue().get("num");
            if (StringUtils.isEmpty(num)){//为空直接返回
                return;
            }
            int result = Integer.parseInt(num);//flag为true则将数据放入缓存
            redisTemplate.opsForValue().set("num",String.valueOf(++result));//

        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            //解锁
            if (lock != null) {
                lock.unlock();
            }
        }


//        //执行setnx
//        //Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", "ok");
//
//        //设置uuid防止误删锁
//        String uuid = UUID.randomUUID().toString();
//        //执行set命令
//        Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock",uuid,1,TimeUnit.SECONDS);//过期时间1秒
//        if (flag){//表示上锁成功
//            /* *
//             * 获取缓存存储的数据
//             *
//             */
//            String num = redisTemplate.opsForValue().get("num");
//            if (StringUtils.isEmpty(num)){//为空直接返回
//                return;
//            }
//            int result = Integer.parseInt(num);//flag为true则将数据放入缓存
//            redisTemplate.opsForValue().set("num",String.valueOf(++result));//
//            /*//执行完成之后，解锁    比较完之后，由于cpu的抢占式策略，可能被其他线程抢到锁，不能保证原子性，
//            if (uuid.equals(redisTemplate.opsForValue().get("lock"))){
//                redisTemplate.delete("lock");//每个线程进来的时候有自己的uuid，如果和分布式锁的key一致，删除锁
//            }*/
//            //解决方式：采用lua脚本删除
//                //定义lua脚本
//            String script="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
//            //赋值lua脚本
//            redisScript.setScriptText(script);
//            redisScript.setResultType(Long.class);
//            redisTemplate.execute(redisScript, Arrays.asList("lock"), uuid);
//
//        }else {//获取锁失败
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            testLockService();//自旋
//        }

    }
    //读锁
    public String readLock() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("lock");
        RLock readLock = readWriteLock.readLock();
        readLock.lock(10,TimeUnit.SECONDS);
        String msg = redisTemplate.opsForValue().get("msg");
        return msg;
    }

    //写锁
    public String writeLock() {

        //获取锁的对象
        RReadWriteLock RReadWriteLock = redissonClient.getReadWriteLock("lock");
        //上锁
        RLock writeLock = RReadWriteLock.writeLock();
        writeLock.lock(10,TimeUnit.SECONDS);//10秒之后自动解锁
        //干活
        String uuid = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("msg",uuid);
        return uuid;
    }
}
