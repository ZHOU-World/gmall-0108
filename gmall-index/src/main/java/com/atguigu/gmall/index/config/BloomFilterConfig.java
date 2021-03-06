package com.atguigu.gmall.index.config;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
/**
 * Date:2021/7/12
 * Author：ZHOU_World
 * Description:redisson布隆过滤器（分布式）
 * 启动时布隆过滤器初始化好,也有数据了
 */
@Configuration
public class BloomFilterConfig {
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private GmallPmsClient pmsClient;

    private static final String KEY_PREFIX="index:cates:";

    @Bean
    public RBloomFilter bloomFilter(){
        RBloomFilter<Object> bloomFilter = this.redissonClient.getBloomFilter("index:bloom:filter");
        bloomFilter.tryInit(1000,0.03);
        //布隆过滤器添加初始数据
       ResponseVo<List<CategoryEntity>> responseVo  = this.pmsClient.queryCategoryByPid(0L);
        List<CategoryEntity> categoryEntities = responseVo.getData();
        if(!CollectionUtils.isEmpty(categoryEntities)){
            categoryEntities.forEach(categoryEntity -> {
                bloomFilter.add(KEY_PREFIX+"["+categoryEntity.getId()+"]");
            });
        }
        return bloomFilter;
    }
}
