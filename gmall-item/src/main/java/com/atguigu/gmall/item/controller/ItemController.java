package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Date:2021/7/14
 * Author：ZHOU_World
 * Description:商品详情页的获取
 */
@Controller
public class ItemController {
    @Autowired
    private ItemService itemService;
    //加载数据（路径为item.gmall.com/{skuId}.html）
    @GetMapping("{skuId}.html")
    //@ResponseBody
    //public ResponseVo<ItemVo> loadData(@PathVariable("skuId")Long skuId){
    public String loadData(@PathVariable("skuId")Long skuId, Model model){
        ItemVo itemVo = this.itemService.loadData(skuId);
        model.addAttribute("itemVo",itemVo);
        //return ResponseVo.ok(itemVo);
        this.itemService.asyncExecute(itemVo);
        return "item";
    }
}
