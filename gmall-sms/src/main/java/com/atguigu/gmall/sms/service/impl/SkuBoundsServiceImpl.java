package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.mapper.SkuFullReductionMapper;
import com.atguigu.gmall.sms.mapper.SkuLadderMapper;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSalesVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {
    @Resource//@Autowired
    SkuFullReductionMapper reductionMapper;
    @Resource//@Autowired
    SkuLadderMapper skuLadderMapper;

    //根据skuId查询营销信息
    @Override
    public List<ItemSaleVo> queryItemSalesBySkuId(Long skuId) {
        ArrayList<ItemSaleVo> itemSaleVos = new ArrayList<>();
        //查询积分优惠（一个商品只有一个积分，可以使用getOne方法）
        SkuBoundsEntity skuBoundsEntity = this.getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id",skuId));
        if(skuBoundsEntity!=null){
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVo.setType("积分");
            itemSaleVo.setDesc("送"+skuBoundsEntity.getGrowBounds()+"成长积分，送"+skuBoundsEntity.getBuyBounds()
                                +"购物积分");
            itemSaleVos.add(itemSaleVo);
        }
        //查询满减
        SkuFullReductionEntity skuFullReductionEntity = this.reductionMapper
                                                            .selectOne(new QueryWrapper<SkuFullReductionEntity>()
                                                            .eq("sku_id",skuId));
        if(skuFullReductionEntity!=null){
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVo.setType("满减");
            itemSaleVo.setDesc("满"+skuFullReductionEntity.getFullPrice()+"减"
                                                +skuFullReductionEntity.getReducePrice());
            itemSaleVos.add(itemSaleVo);
        }
        //查询打折
        SkuLadderEntity skuLadderEntity = this.skuLadderMapper
                                            .selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id",skuId));
        if(skuLadderEntity!=null){
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVo.setType("打折");
            itemSaleVo.setDesc("满"+skuLadderEntity.getFullCount()+"件，打"+
                                skuLadderEntity.getDiscount().divide(new BigDecimal(10))+"折");
            itemSaleVos.add(itemSaleVo);
        }
        return itemSaleVos;
    }

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }

    //保存积分
    @Transactional
    @Override
    public void saleSales(SkuSalesVo saleVo) {
        //3.1保存sms_sku_bounds积分优惠
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(saleVo,skuBoundsEntity);
        List<Integer> work = saleVo.getWork();
        //判断work是否为空且符合格式要求四位
        if(!CollectionUtils.isEmpty(work) || work.size()==4){
            //将work转成十进制的值存入数据库
            Integer works = work.get(3)*2^3+work.get(2)*2^2+work.get(1)*2^1+work.get(0)*2^0;
            skuBoundsEntity.setWork(works);
        }
        this.save(skuBoundsEntity);
        //3.2 保存sms_sku_full_reduction满减
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(saleVo,skuFullReductionEntity);
            //有一个字段需要单独设置，fullAddOther
        skuFullReductionEntity.setAddOther(saleVo.getFullAddOther());
        this.reductionMapper.insert(skuFullReductionEntity);
        //3.3 保存sms_sku_ladder打折
        SkuLadderEntity ladderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(saleVo,ladderEntity);
        ladderEntity.setAddOther(saleVo.getLadderAddOther());
        this.skuLadderMapper.insert(ladderEntity);
    }
}
