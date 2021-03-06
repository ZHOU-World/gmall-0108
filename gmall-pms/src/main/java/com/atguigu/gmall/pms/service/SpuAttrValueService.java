package com.atguigu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author ZHOU_World
 * @email 3361949746@qq.com
 * @date 2021-06-22 21:27:20
 */
public interface SpuAttrValueService extends IService<SpuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    //接口七--根据caregoryId和spuId查询基本类型的搜索类型（search_tupe=1）的规格参数和值
    List<SpuAttrValueEntity> querySearchAttrValuesBySpuId(Long cid, Long spuId);
}

