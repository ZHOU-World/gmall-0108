package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Date:2021/7/13
 * Author：ZHOU_World
 * Description:商品详情页数据模型
 */
@Data
public class ItemVo {
    //面包屑相关信息
    //一二三级分类
    private List<CategoryEntity> categories;
    //品牌信息
    private Long brandId;
    private String brandName;
    //spu商品信息
    private Long spuId;
    private String spuName;

    //中间主体部分
    private Long skuId;
    private String title;
    private String subTitle;
    private BigDecimal price;
    private Integer weight;
    private String defaultImages;
    //营销信息
    private List<ItemSaleVo> sales;
    //是否有货
    private Boolean store=false;
    //sku图片列表
    private List<SkuImagesEntity> images;
    //销售属性列表
    /** 3:白色，黑色
     *  4:8G,12G
     *  5:256G,512G
     */
    private List<SaleAttrValueVo> saleAttrs;
    //当前商品的销售属性（为了进行高亮提示，当属性和当前商品一致时，进行高亮）
    /**
     * 3:白色，4:8G,5:256G
     */
    private Map<Long,String> saleAttr;
    //商品sku与参数组合的映射关系
    //{白色，8G，128G：100 黑色，12G，256G}
    private String skuJsons;

    //商品详情（规格与包装）
    private List<String> spuImages;//图片列表
    //规格参数分组列表
    private List<GroupVo> groups;
}
