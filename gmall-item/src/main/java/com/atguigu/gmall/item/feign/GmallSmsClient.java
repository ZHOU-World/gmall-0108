package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Date:2021/7/14
 * Author：ZHOU_World
 * Description:
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
