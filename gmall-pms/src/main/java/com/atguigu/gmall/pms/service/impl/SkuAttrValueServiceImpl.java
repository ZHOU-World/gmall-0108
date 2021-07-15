package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Resource//@Autowired
    private AttrMapper attrMapper;

    @Resource//@Autowired
    private SkuMapper skuMapper;

    @Resource
    private SkuAttrValueMapper skuAttrValueMapper;
    //根据spuId查询spu下的销售属性和skuId的映射关系
    @Override
    public String queryMappingBySpuId(Long spuId) {
        //先查询spu的列表
        List<SkuEntity> skuEntities = this.skuMapper.selectList(new QueryWrapper<SkuEntity>()
                                        .eq("spu_id",spuId));
        if(CollectionUtils.isEmpty(skuEntities)){
            return null;
        }
        //获取skuId的集合
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());

        //查询映射关系(执行sql语句，获取结果集)s
        List<Map<String,Object>> maps = this.skuAttrValueMapper.queryMappingBySpuId(skuIds);
        if(CollectionUtils.isEmpty(maps)){
            return null;
        }
        //将list集合转化为一个map,通过stream表达式
                                //参数：把什么处理为key,把什么处理为value,对应的类型参数要转换
        Map<String, Long> mappingMap = maps.stream()
                .collect(Collectors.toMap(map -> map.get("attr_values").toString(),
                                            map -> (Long) map.get("sku_id")));
        //将map转化为json字符串
        return JSON.toJSONString(mappingMap);
    }
    //根据spuId查询spuId下所有销售属性的可取值
    @Override
    public List<SaleAttrValueVo> querySaleAttrValuesBySpuId(Long spuId) {
        //查询spu下所有的skuId集合
        List<SkuEntity> skuEntities =  this.skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));
        if(CollectionUtils.isEmpty(skuEntities)){
            return null;
        }
        //获取skuId的集合
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());
        //根据skuIds查询所有的销售属性
        List<SkuAttrValueEntity> skuAttrValueEntities = this.list(new QueryWrapper<SkuAttrValueEntity>()
                                                                    .in("sku_id",skuIds).orderByAsc("attr_id"));
        if(CollectionUtils.isEmpty(skuAttrValueEntities)){
            return null;
        }
        //把结果处理成
        /** attrId:3,attrName:"颜色"，attrValue:[白色，黑色]
         *  attrId:4,attrName:"内存"，attrValue:[8G，12G]
         *  attrId:5,attrName:"机身存储"，attrValue:[128G，256G]
         *  使用stream表达式对结果进行分组，每一组对应一条记录，根据attrId对数据进行分组
         */
        List<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();
        //分组结果以attrId作为Key,以attrId对应的数据作为key
        Map<Long, List<SkuAttrValueEntity>> map = skuAttrValueEntities.stream()
                                                .collect(Collectors.groupingBy(SkuAttrValueEntity::getAttrId));
        //遍历map
        map.forEach((attrId,skuAttrValueEntityList)->{
            //将每一个KV结构转化为SaleAttrValueVo
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            saleAttrValueVo.setAttrId(attrId);
            //有KV结构必然有分组，取第一条记录
            saleAttrValueVo.setAttrName(skuAttrValueEntityList.get(0).getAttrName());
            //再次通过stream表达式将参数值存入数据，使用set,以避免重复问题
            //获取每个分组中的attrValue的set集合
            saleAttrValueVo.setAttrValues(skuAttrValueEntityList.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet()));
            saleAttrValueVos.add(saleAttrValueVo);
        });
        return saleAttrValueVos;
    }

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );
        return new PageResultVo(page);
    }

    //接口六--根据caregoryId和spuId查询基本类型的搜索类型（search_tupe=1）的规格参数和值
    @Override
    public List<SkuAttrValueEntity> querySearchAttrValueBySkuId(Long cid, Long skuId) {
        //1.查询检索类型的规格参数 select * from pms_attr where category_id=225 and search_type=1;
        List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>()
                                                                    .eq("category_id", cid)
                                                                    .eq("search_type", 1));
        //判空
        if(CollectionUtils.isEmpty(attrEntities)){
            return null;
        }
        //2.查询检索类型的规格参数和值select * from pms_sku_attr_value where sku_id=14 and attr_id in (4,5);
                    //规格参数的id集合
        List<Long> attrIds=attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
        return this.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id",skuId).in("attr_id",attrIds));
    }
}
