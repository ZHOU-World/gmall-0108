package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.util.DistributedLock;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Date:2021/7/8
 * Author：ZHOU_World
 * Description:
 */
@Service
public class IndexService {
    //调用远程接口，查询一级分类
    @Resource//@Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;//使用String类型的序列化器，手动序列化
    @Autowired
    private DistributedLock lock;
    @Autowired
    private RedissonClient redissonClient;

    //设置redis数据库的前缀
    private static final String KEY_PREFIX="index:cates:";
    private static final String LOCK_PREFIX="index:cates:lock";

    //获取一级分类
    public List<CategoryEntity> queryLvl1Categories() {
        //根据父分类id查询子分类
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoryByPid(0L);
        //获取数据
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        return categoryEntities;
    }
    //异步请求方法，一级分类下的二级分类中、三级分类的集合
    public List<CategoryEntity> queryLvl2CategoriesByPid(Long pid) {
        //1、先查询缓存，缓存有数据，直接从缓存中获取数据
        String json = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        //判空，不为空，直接返回
        if(StringUtils.isNotBlank(json)){
            return JSON.parseArray(json,CategoryEntity.class);
        }
        //分布式锁解决缓存击穿（热键过期）
        RLock fairLock = this.redissonClient.getFairLock(LOCK_PREFIX + pid);//获取锁
        fairLock.lock();//加锁

        try {
            //在当前请求获取中，可能有其他线程获取到锁，把数据放入到数据库，此时需要再次查看缓存是否存在数据
            String json2 = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
            //判空，不为空，直接返回
            if(StringUtils.isNotBlank(json2)){
                return JSON.parseArray(json2,CategoryEntity.class);
            }
            //2、如果缓存为空，查询数据库获取数据
            ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryLvl2WithSubsByPid(pid);
            List<CategoryEntity> categoryEntities = listResponseVo.getData();
            //3、将数据放入缓存
            //判断获取的数据是否为空
            if(CollectionUtils.isEmpty(categoryEntities)){
                //为了防止缓存穿透，数据即使为空也进行缓存，但缓存时间不能过长
                this.redisTemplate.opsForValue().set(KEY_PREFIX+pid,JSON.toJSONString(categoryEntities),5, TimeUnit.MINUTES);
            }else{
                //为了防止缓存雪崩，给缓存时间添加随机值
                this.redisTemplate.opsForValue().set(KEY_PREFIX+pid,JSON.toJSONString(categoryEntities),
                        180+new Random().nextInt(10), TimeUnit.DAYS);
            }
            return categoryEntities;
        } finally {
            fairLock.unlock();
        }
    }

    //业务代码（使用redisson）
    public void testLock() {
        //加锁（业务逻辑执行之前）
        RLock lock = this.redissonClient.getLock("lock");//获取锁
        lock.lock();//加锁
        try {
            String value = this.redisTemplate.opsForValue().get("num");//获取值，判断是否存在锁
            if(StringUtils.isBlank(value)){//判空
                this.redisTemplate.opsForValue().set("num","1");//设置key和值
            }
            int num = Integer.parseInt(value);
            this.redisTemplate.opsForValue().set("num",String.valueOf(++num));
        } finally {
            //解锁
            lock.unlock();
        }
    }

    //业务代码（分布式锁）
    public void testLock3() {
        //获取锁
        String uuid = UUID.randomUUID().toString();//唯一标识
        //加锁
        Boolean flag = this.lock.lock("lock", uuid, 30);//获取锁
        if(flag){
            String value = this.redisTemplate.opsForValue().get("num");//获取值，判断是否存在锁
            if(StringUtils.isBlank(value)){//判空
                this.redisTemplate.opsForValue().set("num","1");//设置key和值
            }
            int num = Integer.parseInt(value);
            this.redisTemplate.opsForValue().set("num",String.valueOf(++num));

            try {
                TimeUnit.SECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //this.testSub("lock", uuid);
            //解锁
            lock.unlock("lock",uuid);
        }
    }

    //测试是否可重入
    public void testSub(String lockName,String uuid){
        lock.lock(lockName,uuid,30);
        System.out.println("测试可重入");
        lock.unlock(lockName,uuid);
    }

    //测试本地锁
    public void testLock2() {
        String uuid = UUID.randomUUID().toString();//唯一标识
        //获取锁setnx，xxxx是否存在
        Boolean flag = this.redisTemplate.opsForValue().setIfAbsent("lock", "xxxxx",3, TimeUnit.SECONDS);
        //判断
        if(!flag){//值存在，获取锁失败
            try {
                Thread.sleep(100);
                testLock();//重新获取锁
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{//值不存在，获取锁成功，执行业务操作
            String value = this.redisTemplate.opsForValue().get("num");//获取num的值
            if(StringUtils.isBlank(value)){//判空
                this.redisTemplate.opsForValue().set("num","1");//设置key和值
            }
            int num = Integer.parseInt(value);
            this.redisTemplate.opsForValue().set("num",String.valueOf(++num));
            //释放锁
            String script = "if(redis.call('get',KEYS[1])==ARGV[1]) then return redis.call('del',KEYS[1]) else return 0 end";
            this.redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class), Arrays.asList("lock"),uuid);
//            if(StringUtils.equals(uuid,this.redisTemplate.opsForValue().get("lock"))){
  //              this.redisTemplate.delete("lock");
   //         }*/
        }
    }

    //读锁
    public void testRead() {
        //获取读锁
        RReadWriteLock rwLock = this.redissonClient.getReadWriteLock("rwLock");
        rwLock.readLock().lock(10,TimeUnit.SECONDS);
        System.out.println("===测试读锁==");
        //rwLock.readLock().unlock();
    }
    //写锁
    public void testWrite() {
        //获取写锁
        RReadWriteLock rwLock = this.redissonClient.getReadWriteLock("rwLock");
        rwLock.writeLock().lock(10,TimeUnit.SECONDS);
        System.out.println("===测试写锁==");
        //rwLock.writeLock().unlock();
    }
//班长
    public void testLatch() {
        try {
            RCountDownLatch cdl = this.redissonClient.getCountDownLatch("cdl");
            cdl.trySetCount(6);
            cdl.await();
            System.out.println("=========班长锁门=========");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
//同学
    public void testCountDown() {
        RCountDownLatch cdl = this.redissonClient.getCountDownLatch("cdl");
        cdl.countDown();
        System.out.println("=====出来了一位同学======");
    }
}
