package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Date:2021/7/1
 * Author：ZHOU_World
 * Description:
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
