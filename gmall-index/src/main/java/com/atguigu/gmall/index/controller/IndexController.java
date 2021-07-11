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
    @GetMapping("index/cates/{pid}")
    @ResponseBody //异步请求返回json数据，加此注解返回名称
    public ResponseVo<List<CategoryEntity>> queryLvl2CategoriesByPid(@PathVariable("pid") Long pid){
        List<CategoryEntity> categoryEntities = this.indexService.queryLvl2CategoriesByPid(pid);
        return ResponseVo.ok(categoryEntities);
    }

    //测试本地锁
    @GetMapping("index/test/lock")
    @ResponseBody
    public ResponseVo testLock(){
        this.indexService.testLock();
        return ResponseVo.ok();
    }

    //读锁
    @GetMapping("index/test/read")
    @ResponseBody
    public ResponseVo testRead(){
        this.indexService.testRead();
        return ResponseVo.ok();
    }

    //写锁
    @GetMapping("index/test/write")
    @ResponseBody
    public ResponseVo testWrite(){
        this.indexService.testWrite();
        return ResponseVo.ok();
    }

    //班长
    @GetMapping("index/test/Latch")
    @ResponseBody
    public ResponseVo testLatch(){
        this.indexService.testLatch();
        return ResponseVo.ok("班长锁门。。。。");
    }
    //同学出门
    @GetMapping("index/test/countDown")
    @ResponseBody
    public ResponseVo testCountDown(){
        this.indexService.testCountDown();
        return ResponseVo.ok("出来了一位同学。。。");
    }
}
