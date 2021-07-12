package com.atguigu.gmall.index;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallIndexApplicationTests {
    @Autowired
    private RedissonClient redissonClient;
    @Test
    void contextLoads() {
        RBloomFilter<Object> bf = this.redissonClient.getBloomFilter("bf");
        bf.tryInit(20,0.3);
        bf.add("1");
        bf.add("2");
        bf.add("3");
        bf.add("4");
        System.out.println("=======================");
        System.out.println(bf.contains("1"));
        System.out.println(bf.contains("3"));
        System.out.println(bf.contains("5"));
        System.out.println(bf.contains("7"));
        System.out.println(bf.contains("8"));
        System.out.println(bf.contains("19"));
    }

}
