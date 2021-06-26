package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Date:2021/6/25
 * Author：ZHOU_World
 * Description:
 */
@Data
public class SkuVo extends SkuEntity {
    //sms积分优惠字段
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;//单表中是integer,在本类中接收为集合

    //sms打折字段
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther; //为了区分设置为ladderAddOther，原字段为addOther

    //sms满减
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;//为了区分设置为fullAddOther，原字段为addOther

    //图片集合
    private List<String> images;

    //销售属性saleAttr
    private List<SkuAttrValueEntity> saleAttrs;
}
