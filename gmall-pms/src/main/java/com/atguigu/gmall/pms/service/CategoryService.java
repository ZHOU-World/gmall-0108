package com.atguigu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author ZHOU_World
 * @email 3361949746@qq.com
 * @date 2021-06-22 21:27:20
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
    //根据父id查询分类
    List<CategoryEntity> queryCategoryByPid(Long parentId);

    //根据一级分类id(pid)获取二级分类三级分类
    List<CategoryEntity> queryLvl2WithSubsByPid(Long pid);

    //根据三级分类的id(cid)获取一二三级分类
    List<CategoryEntity> queryLvl123WithSubByCid3(Long cid);
}

