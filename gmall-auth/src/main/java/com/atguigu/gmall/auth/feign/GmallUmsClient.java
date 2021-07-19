package com.atguigu.gmall.auth.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Date:2021/7/16
 * Author：ZHOU_World
 * Description:
 */
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi{}