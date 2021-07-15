package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;

import java.util.List;

/**
 * sku销售属性&值
 *
 * @author ZHOU_World
 * @email 3361949746@qq.com
 * @date 2021-06-22 21:27:20
 */
public interface SkuAttrValueService extends IService<SkuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    //接口六--根据caregoryId和spuId查询基本类型的搜索类型（search_tupe=1）的规格参数和值
    List<SkuAttrValueEntity> querySearchAttrValueBySkuId(Long cid, Long skuId);

    //根据spuId查询spuId下所有销售属性的可取值
    List<SaleAttrValueVo> querySaleAttrValuesBySpuId(Long spuId);

    //根据spuId查询spu下的销售属性和skuId的映射关系
    String queryMappingBySpuId(Long spuId);
}

