package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Date:2021/7/3
 * Author：ZHOU_World
 * Description:查询结果集
 */
//@RestController(无页面时)
@Controller
@RequestMapping("search")
public class SearchController {
    @Autowired
    private SearchServiceImpl searchService;

    @GetMapping
    //public ResponseVo<SearchResponseVo> search(SearchParamVo paramVo){
    public String search(SearchParamVo paramVo, Model model){
        SearchResponseVo responseVo = this.searchService.search(paramVo);
        //向前端传递的数据模型
        model.addAttribute("response",responseVo);
        model.addAttribute("searchParam",paramVo);
        return "search";
        //return ResponseVo.ok(responseVo);
    }
}
