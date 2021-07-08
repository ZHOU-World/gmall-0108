package com.atguigu.gmall.pms.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;

import javax.annotation.Resource;

@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Resource//@Autowired
    private CategoryMapper categoryMapper;
    //根据父id查询分类
    @Override
    public List<CategoryEntity> queryCategoryByPid(Long parentId) {
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();

        if(parentId!=-1){//不等于-1,根据条件查询
            wrapper.eq("parent_id",parentId);
        }
        //等于-1，查询所有
        //方法一：通过mapper获取
        // List<CategoryEntity> categoryEntities = this.categoryMapper.selectList(wrapper);
        //方法二：直接通过list,无需mapper
        List<CategoryEntity> categoryEntities = this.list(wrapper);
        return categoryEntities;
    }

    //根据一级分类id(pid)获取二级分类三级分类
    @Override
    public List<CategoryEntity> queryLvl2WithSubsByPid(Long pid) {
        return this.categoryMapper.queryLvl2WithSubsByPid(pid);
    }
}
