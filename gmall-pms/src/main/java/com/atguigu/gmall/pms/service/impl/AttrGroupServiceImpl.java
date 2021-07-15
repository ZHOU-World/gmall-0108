package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.GroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {
    @Resource
    AttrMapper attrMapper;

    @Resource//@Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Resource
    private SpuAttrValueMapper spuAttrValueMapper;

    //根据分类id、spuId、skuId查询出所有的规格参数组及组下的规格参数和值
    @Override
    public List<GroupVo> queryGroupsWithAttrValuesByCidAndSpuIdAndSkuId(Long cid, Long spuId, Long skuId) {
        //根据cid查询分组集合
        List<AttrGroupEntity> groupEntities = this.list(
                new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));
        if(CollectionUtils.isEmpty(groupEntities)){
            return null;
        }
        //遍历分组查询下的规格参数,查询出的集合封装成map
        return groupEntities.stream().map(attrGroupEntity -> {
            GroupVo groupVo = new GroupVo();
            groupVo.setName(attrGroupEntity.getName());
            List<AttrEntity> attrEntities = this.attrMapper.selectList(
                    new QueryWrapper<AttrEntity>().eq("group_id",attrGroupEntity.getId()));
            if(CollectionUtils.isEmpty(attrEntities)){
                //将list<attrEntity>集合转化为list<attrValueVo>
                List<AttrValueVo> attrValueVos = attrEntities.stream().map(attrEntity -> {
                    AttrValueVo attrValueVo = new AttrValueVo();
                    //设置参数
                    attrValueVo.setAttrName(attrEntity.getName());
                    attrValueVo.setAttrId(attrEntity.getId());
                    //区分基本属性和销售属性
                    if(attrEntity.getType()==1){//基本属性
                        SpuAttrValueEntity spuAttrValueEntity = this.spuAttrValueMapper
                                                    .selectOne(new QueryWrapper<SpuAttrValueEntity>()
                                                    .eq("spu_id", spuId)
                                                    .eq("attr_id", attrEntity.getId()));
                        if(spuAttrValueEntity!=null){
                            attrValueVo.setAttrValue(spuAttrValueEntity.getAttrValue());
                        }
                    }else{
                        SkuAttrValueEntity skuAttrValueEntity = this.skuAttrValueMapper
                                                    .selectOne(new QueryWrapper<SkuAttrValueEntity>()
                                                    .eq("sku_id", spuId)
                                                    .eq("attr_id", attrEntity.getId()));
                        if(skuAttrValueEntity!=null){
                            attrValueVo.setAttrValue(skuAttrValueEntity.getAttrValue());
                        }
                    }
                    return attrValueVo;
                }).collect(Collectors.toList());
                groupVo.setAttrs(attrValueVos);
            }
            return groupVo;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    //根据三级分类id查询分组
    @Override
    public List<AttrGroupEntity> queryGroupsWithAttrsByCid(Long cid) {
        //先通过分类cid查询到组列表
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));

        if(CollectionUtils.isEmpty(groupEntities)){//spring框架提供的判断空的方法
            return null;
        }
        //注意，任何 "." 的时候，要考虑是否为空，避免空指针异常
       groupEntities.forEach(attrGroupEntity->{
           //遍历组集合中的元素，再通过组元素查询规格参数
           List<AttrEntity> attrEntities = this.attrMapper.selectList(
                   new QueryWrapper<AttrEntity>().eq("group_id", attrGroupEntity.getId()).eq("type",1));
                attrGroupEntity.setAttrEntities(attrEntities);
       });
        //lambda表达式：（）->{}    ()表示抽象方法中的方法参数，类型+变量  {}表示重写方法后的方法体
        /*for (AttrGroupEntity attrGroupEntity : groupEntities) {
            List<AttrEntity> attrEntities = this.attrMapper.selectList(
                    new QueryWrapper<AttrEntity>().eq("group_id", attrGroupEntity.getId()).eq("type",1));
            attrGroupEntity.setAttrEntities(attrEntities);//给扩展的属性赋值
        }*/

        return groupEntities;
    }


}
