package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSalesVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;

@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {
    @Resource
    SpuDescMapper spuDescMapper;
    @Autowired
    SpuAttrValueService spuAttrValueService;//批量保存使用service
    @Resource
    SkuMapper skuMapper;
    @Autowired
    SkuImagesService skuImagesService;//批量保存使用service
    @Autowired
    SkuAttrValueService skuAttrValueService;
    @Autowired
    GmallSmsClient gmallSmsClient;
    @Autowired
    SpuDescService descService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    //根据检索条件进行分页查询
    @Override
    public PageResultVo querySpuByCidAndPage(PageParamVo paramVo, Long categoryId) {
        //sql语句select * from pms_spu where category_id=255 and (id=7 or name like "%7%"),区分本类和全站
        //封装查询条件
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        if (categoryId != 0) {//categoryId不为0，查本类
            //根据商品分类ID查询,category_id=255
            wrapper.eq("category_id", categoryId);
        }
        String key = paramVo.getKey();//获取关键字
        if (StringUtils.isNotBlank(key)) {//blank判断空格，去空格,--关键字进行查询,
            //wrapper后直接写条件，默认是and连接，并且没有括号
                        //消费性函数式接口，有参数无返回结果集
                        //(id=7 or name like "%7%")
            wrapper.and(t -> t.eq("id", key).or().like("name", key));//t代表的就是wrapper
        }
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                wrapper
        );
        return new PageResultVo(page);
    }

    //大保存方法（九张表）
    @GlobalTransactional
    @Override
    public void bigSave(SpuVo spu) {
        //1、保存spu相关的表（3张）
            //1.1保存pms_spu
        Long spuId = saveSpuInfo(spu);

            //1.2保存pms_spu_desc
        this.descService.saveSpuDesc(spu, spuId);

        //1.3保存pms_spu_attr_value
        saveBaseAttrs(spu, spuId);
        //2、保存sku相关的表（3张）
        saveSkuInfo(spu, spuId);
        this.rabbitTemplate.convertAndSend("PMS_ITEM_EXCHANGE","item.insert",spuId);
    }

    private void saveSkuInfo(SpuVo spu, Long spuId) {
        //2.1 保存pms_sku
        List<SkuVo> skus = spu.getSkus();
        if(CollectionUtils.isEmpty(skus)) {
            return;
        }
        //遍历sku,保存到pms_sku
        skus.forEach(skuVo -> {
            skuVo.setSpuId(spuId);
            skuVo.setCategoryId(spu.getCategoryId());
            skuVo.setBrandId(spu.getBrandId());
            //获取图片列表
            List<String> images = skuVo.getImages();
            //判断是否上传了图片
            if(!CollectionUtils.isEmpty(images)){
                //默认图片(取第一张图片或者后期升级自定义设置默认图片)
                skuVo.setDefaultImage(StringUtils.isNotBlank(skuVo.getDefaultImage())
                        ?images.get(0)
                        :skuVo.getDefaultImage());
            }
            this.skuMapper.insert(skuVo);
            Long skuId = skuVo.getId();

            //2.2 保存pms_sku_images
            //图片集合转变成字符串,先判断是否为空
            if(!CollectionUtils.isEmpty(images)){
                this.skuImagesService.saveBatch(
                        images.stream().map(image->{
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            skuImagesEntity.setUrl(StringUtils.join(image,","));
                            skuImagesEntity.setSkuId(skuId);
                            skuImagesEntity.setUrl(image);
                            //判断是否是默认图片,如果当前图片地址和默认图片地址相同，则是默认图片
                            skuImagesEntity.setDefaultStatus(StringUtils.equals(skuVo.getDefaultImage(),image)
                                    ?1:0);
                            return skuImagesEntity;
                        }).collect(Collectors.toList()));
            }
            //2.3 保存pms_sku_attr_value
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            //判断是否为空
            if(!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(skuAttrValueEntity ->{
                    skuAttrValueEntity.setSkuId(skuId);
                });
                this.skuAttrValueService.saveBatch(saleAttrs);
            }
            //3、保存营销信息相关的3张表
            //需要远程调用
            SkuSalesVo skuSalesVo = new SkuSalesVo();
            BeanUtils.copyProperties(skuVo,skuSalesVo);
            skuSalesVo.setSkuId(skuId);
            this.gmallSmsClient.saleSales(skuSalesVo);
        });
    }

    private Long saveSpuInfo(SpuVo spu) {
        spu.setCreateTime(new Date());
        spu.setUpdateTime(spu.getCreateTime());
        this.save(spu);
        return spu.getId();
    }

    private void saveBaseAttrs(SpuVo spu, Long spuId) {
        List<SpuAttrValueVo> baseAttrs = spu.getBaseAttrs();
        //判断是否为空
        if(!CollectionUtils.isEmpty(baseAttrs)){
            //把SpuAttrValueVo集合转化成SpuAttrValueEntity集合
            List<SpuAttrValueEntity> spuAttrValueEntities= baseAttrs.stream()
                    .filter(spuAttrValueVo -> spuAttrValueVo.getAttrValue()!=null)
                    .map(spuAttrValueVo -> {
                        SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                        //复制属性
                        BeanUtils.copyProperties(spuAttrValueVo,spuAttrValueEntity);
                        spuAttrValueEntity.setSpuId(spuId);
                        return spuAttrValueEntity;
                     }).collect(Collectors.toList());
            this.spuAttrValueService.saveBatch(spuAttrValueEntities);
        }
    }
}
