package com.mt.demo.redissentinel.web;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * WebController
 *
 * @author MT.LUO
 * 2018/10/12 15:59
 * @Description:
 */
@Slf4j
@RestController
public class WebController {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisProperties RedisProperties;

    @Autowired
    private RedissonClient RedissonClient;

    @GetMapping("/test")
    public String test(@RequestParam String key, @RequestParam String value) {
        ValueOperations<String, String> stringRedis = redisTemplate.opsForValue();
        stringRedis.set(key, value);
        return stringRedis.get(key);
    }
    @GetMapping("/get")
    public String get() {
        String key = "redisson_key";
        for (int i = 0; i < 2; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.err.println("=============线程开启============" + Thread.currentThread().getName());
					/*distributedLocker.lock(key,10L); //直接加锁，获取不到锁则一直等待获取锁
					 Thread.sleep(100); //获得锁之后可以进行相应的处理
					 System.err.println("======获得锁后进行相应的操作======"+Thread.currentThread().getName());
					 distributedLocker.unlock(key);  //解锁
					 System.err.println("============================="+Thread.currentThread().getName());*/
                        RLock lock = RedissonClient.getLock(key);
                        System.out.println(lock.isLocked());
                        boolean isGetLock = !lock.isLocked();
                        if (isGetLock) {
                            lock.lock(5L, TimeUnit.SECONDS);
                            Thread.sleep(5000); //获得锁之后可以进行相应的处理
                            System.err.println("======获得锁后进行相应的操作======" + Thread.currentThread().getName());
                            //distributedLocker.unlock(key);
                            System.err.println("=============================" + Thread.currentThread().getName());
                            lock.unlock();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }
        return "s";
    }
}
