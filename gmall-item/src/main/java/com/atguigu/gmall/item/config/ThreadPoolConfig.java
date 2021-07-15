package com.atguigu.gmall.item.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Date:2021/7/15
 * Author：ZHOU_World
 * Description:初始化线程池
 */
@Configuration
public class ThreadPoolConfig {
    //七大重要参数1、corePoolSize：核心线程数
            //2.maximumPoolSize：最大可扩展线程数
            //3.keepAliveTime：生存时间
            //4.unit：时间单位
            //5.workQueue：阻塞队列
            //6.threadFactory：线程工厂
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(
            @Value("${thread.pool.corePoolSize}")Integer coreSize,
            @Value("${thread.pool.maximumPoolSize}")Integer maximumPoolSize,
            @Value("${thread.pool.keepAlive}")Integer keepAlive,
            @Value("${thread.pool.blockingSize}")Integer blockingSize
    ){
        return new ThreadPoolExecutor(coreSize,
                                        maximumPoolSize,
                                        keepAlive,
                                        TimeUnit.SECONDS,
                                        new ArrayBlockingQueue<>(blockingSize));
    }
}
