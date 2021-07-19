package com.atguigu.gmall.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Date:2021/7/17
 * Author：ZHOU_World
 * Description:全局过滤器
 */
@Component
public class MyGloblaFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //拦截业务逻辑
        System.out.println("全局过滤器，无差别拦截所有经过网关的请求");
        //放行
        return chain.filter(exchange);
    }
}
