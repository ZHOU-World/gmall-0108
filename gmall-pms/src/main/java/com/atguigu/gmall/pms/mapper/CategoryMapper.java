package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品三级分类
 * 
 * @author ZHOU_World
 * @email 3361949746@qq.com
 * @date 2021-06-22 21:27:20
 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryEntity> {
    //根据一级分类id(pid)获取二级分类三级分类
    List<CategoryEntity> queryLvl2WithSubsByPid(Long pid);
}
