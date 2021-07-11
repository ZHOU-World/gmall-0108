package com.atguigu.gmall.index.config;

import java.lang.annotation.*;

/**
 * Date:2021/7/11
 * Author：ZHOU_World
 * Description:自定义注解
 */
@Target({ ElementType.METHOD})//作用在类上
@Retention(RetentionPolicy.RUNTIME)//运行时注解
@Documented//是否需要添加到文档中
public @interface GmallCache {
    /**
     * 自定义缓存的前缀
     * @return
     */
    String prefix() default "gmall";

    /**
     * 缓存的过期时间，单位分钟
     * @return
     */
    int timeout() default 30;//过期时间

    /**
     * 防止缓存雪崩，给缓存时间添加随机值，随机范围
     * 单位分钟
     * @return
     */
    int random() default 30;
    /**
     * 防止缓存击穿，指定分布式锁
     * 此处为分布式锁的前缀
     */
    String lock() default "lock";
}
