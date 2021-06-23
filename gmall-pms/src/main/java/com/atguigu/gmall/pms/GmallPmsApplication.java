package com.atguigu.gmall.pms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableFeignClients //远程调用
@EnableDiscoveryClient //注册中心
@EnableSwagger2 //swagger
@MapperScan("com.atguigu.gmall.pms.mapper") //扫描
public class GmallPmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallPmsApplication.class, args);
    }
}
