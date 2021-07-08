package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Date:2021/6/23
 * Author：ZHOU_World
 * Description:配置类，解决跨域问题
 */
@Configuration
public class CrosConfig {
    @Bean
    public CorsWebFilter corsWebFilter(){
        //配置
        CorsConfiguration config = new CorsConfiguration();
            //允许所有请求方式跨域访问
        config.addAllowedMethod("*");
            //允许跨域访问的域名，*代表所有，无法携带cookie
        config.addAllowedOrigin("http://manager.gmall.com");
        config.addAllowedOrigin("http://www.gmall.com");
        config.addAllowedOrigin("http://gmall.com");
            //允许跨域访问的头信息
        config.addAllowedHeader("*");
            //允许携带cookie,如果允许了，Origin不能写成*
        config.setAllowCredentials(true);
        //注册类，拦截所有请求，进行cors验证，注意，是响应式的，reactive
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //配置源对象
        source.registerCorsConfiguration("/**",config);
        return new CorsWebFilter(source);
    }
}
