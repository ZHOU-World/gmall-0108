package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValueVo;
import com.atguigu.gmall.search.repository.GoodsRespository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Date:2021/7/7
 * Author：ZHOU_World
 * Description:
 */
@Component //不知道属于哪一层的组件，直接使用component
public class ItemListener {
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GoodsRespository goodsRespository;

    @RabbitListener(bindings = @QueueBinding(
            value=@Queue("SEARCH_ITEM_QUEUE"),
            exchange = @Exchange(value="PMS_ITEM_EXCHANGE",ignoreDeclarationExceptions ="true",type = ExchangeTypes.TOPIC),
            key = {"item.insert"}
    ))
    public void listen(Long spuId, Channel channel, Message message) throws IOException {
        //判断spuId是否为空,为空即为垃圾数据，确认掉，不进行处理
        if(spuId==null){                                                       //是否批量确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }
        //不是垃圾消息，完成数据同步
        //根据spuId查询spu详情信息，调用远程接口
        ResponseVo<SpuEntity> spuEntityResponseVo = this.pmsClient.querySpuById(spuId);
        SpuEntity spuEntity = spuEntityResponseVo.getData();
        if(spuEntity==null){//判断如果spuId查询的结果为空，无效数据，确认消息，不进行任何处理
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }
        //通过spuId查询sku规格参数的返回值
        ResponseVo<List<SkuEntity>> skuResponseVo = this.pmsClient.querySkusBySpuId(spuId);
        //获取集合
        List<SkuEntity> skuEntities = skuResponseVo.getData();
//        try{
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
            //消费者确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
//        }catch (Exception e){
//            //判断是否为重复投递信息
//            if(message.getMessageProperties().getRedelivered()){
//                //拒绝消息                                                           //是否重新入队
//                channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
//            }else{
//                //不确认                                                        //是否批量确认  //是否重新入队
//                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
//            }
//        }
    }
}
