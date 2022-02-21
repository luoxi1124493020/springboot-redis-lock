package com.luoxi.springbootredislock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/redisTest")
public class RedisTestController {

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping
    public String testRedis()
    {
        redisTemplate.opsForValue().set("spring","h1");
        System.out.println("redis get success");
        System.out.println("its fine");
        System.out.println("this is hot fix ");
        System.out.println("master change!!!");
        System.out.println("hot fix change!!!!");
        String spring = (String) redisTemplate.opsForValue().get("spring");
        return spring;
    }

    @GetMapping("/testLock")
    public void testLock(){
        //1获取锁，setne
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid,3, TimeUnit.SECONDS);
        //2获取锁成功、查询num的值
        if(lock){
            Object value = redisTemplate.opsForValue().get("num");
            //2.1判断num为空return
            if(StringUtils.isEmpty(value)){
                return;
            }
            //2.2有值就转成成int
            int num = Integer.parseInt(value+"");
            //2.3把redis的num加1
            redisTemplate.opsForValue().set("num", ++num);
            String lockUUID = (String) redisTemplate.opsForValue().get(lock);
            if(uuid.equals(lockUUID))
            {
                //2.4释放锁，del
                redisTemplate.delete("lock");
            }
        }else{
            //3获取锁失败、每隔0.1秒再获取
            try {
                Thread.sleep(100);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
