package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.interceptor.LoginIntercepter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Date:2021/7/19
 * Author：ZHOU_World
 * Description:控制器
 */
public class CartController {
    @GetMapping("test")
    @ResponseBody
    public String test(HttpServletRequest request){
        //System.out.println(request.getAttribute("userKey"));
        System.out.println(LoginIntercepter.getUserInfo());
        return "test";
    }
}
