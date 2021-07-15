package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 * 
 * @author ZHOU_World
 * @email 3361949746@qq.com
 * @date 2021-06-22 21:27:20
 */
@Mapper
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValueEntity> {
    //根据spuId查询spu下的销售属性和skuId的映射关系
        //注意：基本数据类型的参数，通过任意参数都可以接收
            //集合情况下需起别名
    List<Map<String, Object>> queryMappingBySpuId(@Param("skuIds")List<Long> skuIds);
}
