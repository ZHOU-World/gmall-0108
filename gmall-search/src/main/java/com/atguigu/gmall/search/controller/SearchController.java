package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Date:2021/7/3
 * Author：ZHOU_World
 * Description:查询结果集
 */
@RestController
@RequestMapping("search")
public class SearchController {
    @Autowired
    private SearchServiceImpl searchService;

    @GetMapping
    public ResponseVo<SearchResponseVo> search(SearchParamVo paramVo){
        SearchResponseVo responseVo = this.searchService.search(paramVo);
        return ResponseVo.ok(responseVo);
    }
}
