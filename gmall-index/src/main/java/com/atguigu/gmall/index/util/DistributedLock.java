package com.atguigu.gmall.index.util;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Date:2021/7/11
 * Author：ZHOU_World
 * Description:加锁解锁
 */
@Component//不知道属于哪一层，就使用Component
public class DistributedLock {
    @Autowired
    private StringRedisTemplate redisTemplate;
    //设置全局变量
    private Timer timer;

    //加锁
    public Boolean lock(String lockName,String uuid,Integer expire){
        //添加加锁的lua脚本
        String script = "if (redis.call('exists' ,KEYS[1])==0 or redis.call('hexists',KEYS[1],ARGV[1])==1) " +
                "then " +
                "   redis.call('HINCRBY',KEYS[1],ARGV[1],1)" +
                "   redis.call('expire',KEYS[1],ARGV[2]) " +
                "   return 1 " +
                "else " +
                "   return 0 " +
                "end";
        //执行lua脚本
                                                    //因为使用了String类型的redisTemplate，任何数据都是String类型
        Boolean flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class),
                                                Arrays.asList(lockName), uuid, expire.toString());
        if(!flag){
            try{
                Thread.sleep(100);
                lock(lockName,uuid,expire);//重新获取锁
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }else{
            this.renewExpire(lockName,uuid,expire);
        }
        return true;
    }

    //解锁
    public void unlock(String lockName, String uuid){
        //添加解锁的lua脚本
        String script = "if(redis.call('hexists',KEYS[1],ARGV[1])==0) " +
                "then " +
                "   return nil " +
                "elseif (redis.call('HINCRBY',KEYS[1],ARGV[1],-1) == 0) " +
                "then " +
                "   return redis.call('del',KEYS[1]) " +
                "else " +
                "   return 0 " +
                "end";
        //执行lua脚本                              返回值类型不要使用boolean,返回空在java中也是false,不具体，推荐使用Long类型
        Long flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                Arrays.asList(lockName), uuid);
        if(flag==null){//代表任意释放锁，抛出异常
            throw new IllegalArgumentException("锁不是你的锁");
        }else if(flag ==null){
            //释放锁成功的情况下，取消定时任务
            this.timer.cancel();
        }
    }

    //自动续期
    private void renewExpire(String lockName, String uuid,Integer expire){
       String script = "if(redis.call('hexists',KEYS[1],ARGV[1])==1) " +
               "then redis.call('expire',KEYS[1],ARGV[2]) " +
               "end";
         this.timer =new Timer();
         this.timer.schedule(new TimerTask() {
           @Override
           public void run() {
               redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class),
                       Arrays.asList(lockName),uuid,expire.toString());//注意：换成String类型的
           }
       },expire*1000/3,expire*1000/3);//注意：单位是毫秒

    }
    //定时器
//    public static void main(String[] args){
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                System.out.println("定时任务："+System.currentTimeMillis());
//            }
//        },5000,10000);//延迟5秒钟，每10秒钟执行一次
//    }

    //布隆过滤器
    public static void main(String[] args) {
        //第一个参数：放入二级制数组的元素类型
        //第二个参数：期望数组中元素的数量
        //第三个参数：误判率
        BloomFilter<CharSequence> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 30, 0.3);
        //向布隆过滤其中放入元素
        bloomFilter.put("10");
        bloomFilter.put("20");
        bloomFilter.put("30");
        bloomFilter.put("40");
        bloomFilter.put("50");
        bloomFilter.put("60");
        bloomFilter.put("70");
        bloomFilter.put("80");
        System.out.println("====================");
        //判断数据是否存在
        System.out.println(bloomFilter.mightContain("10"));
        System.out.println(bloomFilter.mightContain("40"));
        System.out.println(bloomFilter.mightContain("80"));
        System.out.println(bloomFilter.mightContain("100"));
        System.out.println(bloomFilter.mightContain("101"));
        System.out.println(bloomFilter.mightContain("105"));
        System.out.println(bloomFilter.mightContain("110"));
        System.out.println(bloomFilter.mightContain("130"));
        System.out.println(bloomFilter.mightContain("145"));
    }
}
