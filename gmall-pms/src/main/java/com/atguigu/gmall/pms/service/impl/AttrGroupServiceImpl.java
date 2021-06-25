package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }
    @Resource
    AttrMapper attrMapper;
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
