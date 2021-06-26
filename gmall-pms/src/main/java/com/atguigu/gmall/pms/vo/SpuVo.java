package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

/**
 * Date:2021/6/25
 * Author：ZHOU_World
 * Description:页面交互数据类（最外层数据结构）
 * 数据结构与SpuEntity类似，直接继承SpuEntity
 */
@Data
public class SpuVo extends SpuEntity {
    private List<String> spuImages;
    private List<SpuAttrValueVo> baseAttrs;
    private List<SkuVo> skus;
}
