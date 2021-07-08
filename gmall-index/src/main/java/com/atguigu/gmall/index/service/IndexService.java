package com.atguigu.gmall.index.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Date:2021/7/8
 * Author：ZHOU_World
 * Description:
 */
@Service
public class IndexService {
    //调用远程接口，查询一级分类
    @Resource//@Autowired
    private GmallPmsClient pmsClient;
//获取一级分类
    public List<CategoryEntity> queryLvl1Categories() {
        //根据父分类id查询子分类
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoryByPid(0L);
        //获取数据
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        return categoryEntities;
    }

    //异步请求方法，一级分类下的二级分类中、三级分类的集合
    public List<CategoryEntity> queryLvl2CategoriesByPid(Long pid) {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryLvl2WithSubsByPid(pid);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        return categoryEntities;
    }
}
