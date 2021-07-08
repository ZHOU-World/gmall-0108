package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Date:2021/7/8
 * Author：ZHOU_World
 * Description:跳转至首页
 */
@Controller
public class IndexController{
    @Autowired
    private IndexService indexService;

    @GetMapping
    public String toIndex(Model model){

        //获取一级分类（异步查询）
        List<CategoryEntity> categoryEntityList = this.indexService.queryLvl1Categories();
        //前端页面接收数据为categories
        model.addAttribute("categories",categoryEntityList);
        return "index";//返回到首页页面
    }

    //异步请求方法，一级分类下的二级分类中、三级分类的集合
    @GetMapping("index/cates//{pid}")
    @ResponseBody //异步请求返回json数据，加此注解返回名称
    public ResponseVo<List<CategoryEntity>> queryLvl2CategoriesByPid(@PathVariable("pid") Long pid){
        List<CategoryEntity> categoryEntities = this.indexService.queryLvl2CategoriesByPid(pid);
        return ResponseVo.ok(categoryEntities);
    }
}
