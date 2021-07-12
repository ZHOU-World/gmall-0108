package com.atguigu.gmall.index;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.config.GmallCache;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Date:2021/7/12
 * Author：ZHOU_World
 * Description:通知方法测试
 */
@Aspect
@Component
public class GmallCacheAspect {
//    @Pointcut("execution(* com.atguigu.gmall.index.service.*.*(..))")
//    public void pointcut() {}
//
//    @Before("pointcut()")
//    public void before(JoinPoint joinPoint){
//        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
//        System.out.println("前置方法类的类名"+joinPoint.getTarget().getClass().getName());
//        System.out.println("目标方法名"+signature.getMethod().getName());
//        System.out.println("目标方法的参数列表"+joinPoint.getArgs());
//    }
//
//    @AfterReturning(value = "pointcut()",returning = "result")
//    public void afterReturning(JoinPoint joinPoint,Object result){
//        System.out.println("返回值通知" );
//    }
//
//    @AfterThrowing(value="pointcut()",throwing = "ex")
//    public void afterThrowing(JoinPoint joinPoint,Exception ex){
//        System.out.println("异常通知");
//    }
//
//    @After("pointcut()")
//    public void after(){
//        System.out.println("最终通知");
//    }
//}
//    @Around("pointcut()")
//    public Object aroundceshi(ProceedingJoinPoint joinPoint) throws Throwable{
//        System.out.println("前增强");
//        //手动执行目标方法
//        Object result = joinPoint.proceed(joinPoint.getArgs());
//        System.out.println("后增强");
//        return result;
//    }

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;//使用redisson框架实现分布式锁
    @Autowired
    private RBloomFilter bloomFilter;
    //通过拦截注解的方式拦截，只拦截带@GmallCache的方法
    @Around("@annotation(com.atguigu.gmall.index.config.GmallCache)")

    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取目标方法的签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取目标方法
        Method method = signature.getMethod();
        //获取目标方法上GmallCache注解
        GmallCache gmallCache = method.getAnnotation(GmallCache.class);
        //获取目标方法的参数列表
        List<Object> args = Arrays.asList(joinPoint.getArgs());
        //获取注解中的前缀
        String prefix = gmallCache.prefix();
        //组装前缀,注意：数组+字符串，会调用数组的toString方法，数组返回的是地址
        String key = prefix + args;
        //获取返回值类型
        Class returnType = signature.getReturnType();
        //解决缓存穿透问题，使用布隆过滤器先进行过滤
        if(!this.bloomFilter.contains(key)){//不包含，说明数据不存在，不进行处理
            return null;
        }
        //1、先查询缓存，如果缓存中有数据，直接返回
        String json = this.redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(json)) {
            return JSON.parseObject(json, returnType);
        }
        //2、如果缓存中没有数据，返回值为空，为了防止缓存击穿，添加分布式锁
        RLock fairLock = this.redissonClient.getFairLock(gmallCache.lock() + args);
        fairLock.lock();//加锁
        Object result;
        try {
            //再次查询缓存，在获取分布式锁的时候，有其他请求将数据放入了缓存
            String json2 = this.redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(json2)) {
                return JSON.parseObject(json2, returnType);
            }
            //手动执行目标方法
            result = joinPoint.proceed(joinPoint.getArgs());
            //将目标方法的返回值放入缓存中（缓存穿透，缓存雪崩）
            //设置缓存时间
            if(result!=null){//防止缓存雪崩（热键集体失效）
                int timeout = gmallCache.timeout()+new Random().nextInt(gmallCache.random());
                this.redisTemplate.opsForValue().set(key,JSON.toJSONString(result),timeout, TimeUnit.MINUTES);
            }
            //通过布隆过滤器防止缓存穿透（查询不存在的值）（后面讲）
            return result;
        } finally {
            fairLock.unlock();//解锁
        }
    }
}
