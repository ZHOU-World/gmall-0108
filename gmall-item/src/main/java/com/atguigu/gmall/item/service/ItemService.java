package com.atguigu.gmall.item.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.ItemException;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
/**
 * Date:2021/7/14
 * Author：ZHOU_World
 * Description:
 */
@Service
public class ItemService {
    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    public ItemVo loadData(Long skuId) {
        ItemVo itemVo = new ItemVo();
        //一一获取参数,通过远程调用接口

        //1.根据skuId查询sku
        //初始化线程任务
        CompletableFuture<SkuEntity> skuFuture = CompletableFuture.supplyAsync(() -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                throw new ItemException("skuId对应的商品不存在");
            }
            //页面中间详情信息
            itemVo.setSkuId(skuId);
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setSubTitle(skuEntity.getSubtitle());
            itemVo.setDefaultImages(skuEntity.getDefaultImage());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setWeight(skuEntity.getWeight());
            return skuEntity;
        }, threadPoolExecutor);


        //2.根据三级分类Id查询一二三级分类
        //线程任务依赖任务skuFuture
        CompletableFuture<Void> catesFuture = skuFuture.thenAcceptAsync((skuEntity) -> {
            ResponseVo<List<CategoryEntity>> catesResponseVo = this.pmsClient.queryLvl123WithSubByCid3(
                    skuEntity.getCategoryId());
            List<CategoryEntity> categoryEntities = catesResponseVo.getData();
            itemVo.setCategories(categoryEntities);
        }, threadPoolExecutor);

        //3.根据品牌id查询品牌
        //线程任务依赖任务skuFuture
        CompletableFuture<Void> brandFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<BrandEntity> brandsEntityResponseVo = this.pmsClient.queryBrandById(skuEntity.getBrandId());
            BrandEntity brandEntity = brandsEntityResponseVo.getData();
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());
            }
        }, threadPoolExecutor);

        //4.根据spuId查询SPU
        //线程任务依赖任务skuFuture
        CompletableFuture<Void> spuFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuEntity> spuEntityResponseVo = this.pmsClient.querySpuById(skuEntity.getSpuId());
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            if (spuEntity != null) {
                itemVo.setSpuId(spuEntity.getId());
                itemVo.setSpuName(spuEntity.getName());
            }
        }, threadPoolExecutor);

        //5.根据skuId查询营销信息
        //不依赖任何线程结果
        CompletableFuture<Void> salesFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<ItemSaleVo>> salesResponseVo = this.smsClient.queryItemSalesBySkuId(skuId);
            List<ItemSaleVo> itemSaleVos = salesResponseVo.getData();
            itemVo.setSales(itemSaleVos);
        }, threadPoolExecutor);

        //6.根据skuId查询库存列表(是否有货)
        //不依赖任何线程结果
        CompletableFuture<Void> wareFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<WareSkuEntity>> wareResponseVo = this.wmsClient.queryWareSkusBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                itemVo.setStore(wareSkuEntities.stream()
                        .anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
        }, threadPoolExecutor);

        //7.根据skuId查询sku的图片列表
        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuImagesEntity>> imagesResponseVo = this.pmsClient.querySkuImagesBySkuId(skuId);
            List<SkuImagesEntity> skuImagesEntities = imagesResponseVo.getData();
            itemVo.setImages(skuImagesEntities);
        }, threadPoolExecutor);

        //8.根据spuId查询spu下所有销售属性的可取值
        //线程任务依赖任务skuFuture
        CompletableFuture<Void> saleAttrs = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<SaleAttrValueVo>> saleAttrsResponseVo = this.pmsClient.querySaleAttrValuesBySpuId(
                    skuEntity.getSpuId());
            List<SaleAttrValueVo> saleAttrValueVo = saleAttrsResponseVo.getData();
            itemVo.setSaleAttrs(saleAttrValueVo);
        }, threadPoolExecutor);

        //9.根据skuId查询当前sku的销售属性
        CompletableFuture<Void> saleAttrFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuAttrValueEntity>> saleAttrReponseVo = this.pmsClient.querySkuAttrValueBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrReponseVo.getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                //把集合处理成map格式,将值设置为item中的销售属性
                itemVo.setSaleAttr(skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId,
                        SkuAttrValueEntity::getAttrValue)));
            }
        }, threadPoolExecutor);

        //10.根据spuId所有销售属性组合和skuId的映射关系
        //线程任务依赖任务skuFuture
        CompletableFuture<Void> mappingFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<String> stringResponseVo = this.pmsClient.queryMappingBySpuId(skuEntity.getSpuId());
            String json = stringResponseVo.getData();
            itemVo.setSkuJsons(json);
        }, threadPoolExecutor);


        //11.根据spuId查询spu的描述信息
        //线程任务依赖任务skuFuture
        CompletableFuture<Void> descFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuDescEntity> spuDescEntityResponseVo = this.pmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();
            //描述信息为图片信息，需要将其逗号分隔转到List集合中
            if (spuDescEntity != null) {
                String decript = spuDescEntity.getDecript();
                itemVo.setSpuImages(Arrays.asList(StringUtils.split(spuDescEntity.getDecript(), ",")));
            }
        }, threadPoolExecutor);

        //12.根据分类id、spuId、skuId查询出所有的规格参数组及组下的规格参数和值
        //线程任务依赖任务skuFuture
        CompletableFuture<Void> groupFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<GroupVo>> groupResponseVo = this.pmsClient
                    .queryGroupsWithAttrValuesByCidAndSpuIdAndSkuId(skuEntity.getCategoryId(), skuEntity.getSpuId(), skuId);
            List<GroupVo> groupVos = groupResponseVo.getData();
            itemVo.setGroups(groupVos);

        }, threadPoolExecutor);

        //全部都执行完，返回itemVo结果
        CompletableFuture.allOf(catesFuture,brandFuture,spuFuture,salesFuture,wareFuture,imagesFuture,
                saleAttrs,saleAttrFuture,mappingFuture,descFuture,groupFuture).join();
        return itemVo;
    }




    //new MyThread().start();
    /**实现runable接口匿名内部类

     new Thread(new Runnable() {
    @Override public void run() {
    System.out.println("实现runable接口初始化" + Thread.currentThread().getName());
    }
    }).start();
     */
    /**lambda表达式
     new Thread(()->{
     System.out.println("lambda表达式初始化"+Thread.currentThread().getName());
     }).start();
     System.out.println("main线程" + Thread.currentThread().getName());*/
    /**callback+futureTask
     new Thread(new FutureTask<String>(()->{
     System.out.println("callable实现初始化"+Thread.currentThread().getName());
     return ".........";
     })).start();
     System.out.println("mian主线程");*/
    /**
     * 线程池
     * ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor
     * //七大重要参数1、corePoolSize：核心线程数
     * //			2.maximumPoolSize：最大可扩展线程数
     * //			3.keepAliveTime：生存时间
     * //			4.unit：时间单位
     * //			5.workQueue：阻塞队列
     * //			6.threadFactory：线程工厂
     * //7.handler：拒绝策略
     * (3, 5, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));
     * //lambda表达式编写
     * threadPoolExecutor.execute(()->
     * System.out.println("线程池初始化多线程"));
     * //正常编写
     * threadPoolExecutor.execute(new Runnable() {
     *
     * @Override public void run() {
     * System.out.println("线程池初始化多线程2");
     * }
     * });
     * System.out.println("主线程");
     */
    public static void main(String[] args) throws IOException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("ComplateableFuture-supplyAsync初始化一个多线程程序");
            //int i = 1/0;
            return "这是ComplateableFuture方法";
        });
        CompletableFuture<String> future1 = future.thenApplyAsync((t) -> {
            System.out.println("=======thenApplyAsync=========");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("上一个任务返回的结果集t:" + t);
            return "这是thenApplyAsync方法";
        });
        CompletableFuture<Void> future2 = future.thenAcceptAsync(t -> {
            System.out.println("=======thenAcceptAsync=======");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("上一个任务返回的结果集t:" + t);
        });
        CompletableFuture<Void> future3 = future.thenRunAsync(() -> {//既没有参数也没有返回结果集
            System.out.println("=======thenRun=======");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("thenRunAsync方法");
        });
        CompletableFuture<Void> future4 = CompletableFuture.runAsync(() -> {

            System.out.println("新任务");
        });
        CompletableFuture.anyOf(future1,future2,future3,future4).join();
//                .whenCompleteAsync((t, u) -> {
//            System.out.println("=======whenCompleteAsync=========");
//            System.out.println("上一个任务返回的结果集t:" + t);
//            System.out.println("上一个任务返回的异常信息u:" + u);
//        }).exceptionally((t) -> {
//            System.out.println("==========exceptionally==========");
//            System.out.println("exceptionlly中的t:" + t);
//            return null;
        //});
        System.out.println("主线程");
        System.in.read();
    }
}

//多线程回顾
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("继承Thread实现初始化" + Thread.currentThread().getName());
    }
}
