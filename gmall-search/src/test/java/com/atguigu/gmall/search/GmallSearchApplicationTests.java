package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValueVo;
import com.atguigu.gmall.search.repository.GoodsRespository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.junit.jupiter.api.Test;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {
    @Autowired
    private ElasticsearchRestTemplate restTemplate;
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GoodsRespository goodsRespository;
    @Test
    void contextLoads() {
        //判断是否有goods索引库
        if(! this.restTemplate.indexExists(Goods.class)){
            //声明索引库
            this.restTemplate.createIndex(Goods.class);
            //声明映射
            this.restTemplate.putMapping(Goods.class);
        }
        Integer pageNum = 1;//第一页
        Integer pageSize = 100;//100条数据
        do{
            //创建page对象
            PageParamVo pageParamVo = new PageParamVo(pageNum, pageSize, null);
            //分批查询spu
            ResponseVo<List<SpuEntity>> responseVo = this.pmsClient.querySpuByPageJson(pageParamVo);//每页数据
            List<SpuEntity> spuEntities = responseVo.getData();

            if(CollectionUtils.isEmpty(spuEntities)) {//如果spu的数量是100的整数倍，最后一次查询结果为空
                return;//停止查询
            }
            //遍历spu查询spu下所有的sku
            spuEntities.forEach(spuEntity -> {
                //通过spuId查询sku规格参数的返回值
                ResponseVo<List<SkuEntity>> skuResponseVo = this.pmsClient.querySkusBySpuId(spuEntity.getId());
                //获取集合
                List<SkuEntity> skuEntities = skuResponseVo.getData();
                if(!CollectionUtils.isEmpty(skuEntities)){
                    //查询品牌和分类，在遍历sku之前进行查询,同一个spu品牌和分类都一样
                    ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(spuEntity.getBrandId());
                    BrandEntity brandEntity = brandEntityResponseVo.getData();//获取品牌集合
                    ResponseVo<CategoryEntity> categoryEntityResponseVo = this.pmsClient.queryCategoryById(spuEntity.getCategoryId());
                    CategoryEntity categoryEntity = categoryEntityResponseVo.getData();//获取分类集合
                    //将list集合转化为goods集合,导入到es
                        List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                            Goods goods = new Goods();
                            //将skuEntity的值赋值给goods对象

                            //设置商品列表信息
                            goods.setSkuId(skuEntity.getId());
                            goods.setDefaultImage(skuEntity.getDefaultImage());
                            goods.setTitle(skuEntity.getTitle());
                            goods.setSubTitle(skuEntity.getSubtitle());
                            goods.setPrice(skuEntity.getPrice().doubleValue());

                            //设置创建时间
                            goods.setCreateTime(spuEntity.getCreateTime());

                            //销量和库存（调用远程接口）
                            //通过sku的id查询
                            ResponseVo<List<WareSkuEntity>> wareResponseVo = this.wmsClient.queryWareSkusBySkuId(skuEntity.getId());
                            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();//通过返回值获取结果
                            //判空
                            if(!CollectionUtils.isEmpty(wareSkuEntities)){
                                //获取销量集合（mapLong直接获取值为int的销量集合）
                                goods.setSales(wareSkuEntities.stream().mapToLong(WareSkuEntity::getSales)
                                        .reduce((a,b)->a+b).getAsLong());
                                //设置库存是否有货,库存-已锁库存 > 0
                                goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity ->
                                        wareSkuEntity.getStock()-wareSkuEntity.getStockLocked()>0));

                            }

                            //判断，品牌不为空，设置品牌
                            if(brandEntity!=null){
                                goods.setBrandId(brandEntity.getId());
                                goods.setBrandName(brandEntity.getName());
                                goods.setLogo(brandEntity.getLogo());
                            }
                            //分类
                            if(categoryEntity!=null){
                                goods.setCategoryId(categoryEntity.getId());
                                goods.setCategoryName(categoryEntity.getName());
                            }

                            //规格参数聚合
                            //创建检索类型的规格参数集合
                            ArrayList<SearchAttrValueVo> attrValueVos = new ArrayList<>();
                            //远程调用获取参数
                            //sku中的规格参数
                            ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = this.pmsClient.querySearchAttrValueBySkuId
                                    (skuEntity.getCategoryId(), skuEntity.getId());
                            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();//获取销售类型的规格集合
                            //判空,不为空将集合转换为SearchAttrValueVo类型的集合
                            if(!CollectionUtils.isEmpty(skuAttrValueEntities)){
                                List<SearchAttrValueVo> collect = skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                                    SearchAttrValueVo attrValueVo = new SearchAttrValueVo();
                                    BeanUtils.copyProperties(skuAttrValueEntity, attrValueVo);
                                    return attrValueVo;
                                }).collect(Collectors.toList());
                                attrValueVos.addAll(collect);//将集合全部放进attrValueVos集合中
                            }
                            //spu中的规格参数
                            ResponseVo<List<SpuAttrValueEntity>> baseAttrResponseVo = this.pmsClient.querySearchAttrValuesBySpuId(
                                    skuEntity.getCategoryId(), skuEntity.getId());
                            List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrResponseVo.getData();//获取基本类型的规格集合
                            //判空,不为空将集合转换为SearchAttrValueVo类型的集合
                            if(!CollectionUtils.isEmpty(spuAttrValueEntities)){
                                attrValueVos.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                    SearchAttrValueVo attrValueVo = new SearchAttrValueVo();
                                    BeanUtils.copyProperties(spuAttrValueEntity, attrValueVo);
                                    return attrValueVo;
                                }).collect(Collectors.toList()));//将集合全部放进attrValueVos集合中
                            }
                            //设置规格参数属性
                            goods.setSearchAttrs(attrValueVos);

                            return goods;
                        }).collect(Collectors.toList());
                    this.goodsRespository.saveAll(goodsList);
                }
            });
            //不是最后一页，pageSize为100，继续循环，正好为100，在查询一次，下一页pageSize为零;是最后一页，pageSize不足100，在while循环中停止循环
            pageSize = spuEntities.size();
            pageNum++;
        }while(pageSize==100);
    }
}
