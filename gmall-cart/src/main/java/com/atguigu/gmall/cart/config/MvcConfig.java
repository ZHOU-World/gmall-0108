package com.atguigu.gmall.cart.config;

import com.atguigu.gmall.cart.interceptor.LoginIntercepter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Date:2021/7/19
 * Author：ZHOU_World
 * Description:springboot拦截器
 */
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginIntercepter loginIntercepter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginIntercepter).addPathPatterns("/**");
    }
}
