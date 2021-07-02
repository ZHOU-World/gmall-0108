package com.atguigu.gmall.search.repository;

import com.atguigu.gmall.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Date:2021/7/1
 * Author：ZHOU_World
 * Description:
 */
public interface GoodsRespository extends ElasticsearchRepository<Goods,Long> {

}
