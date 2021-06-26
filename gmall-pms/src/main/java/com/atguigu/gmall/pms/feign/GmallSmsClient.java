package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Date:2021/6/27
 * Author：ZHOU_World
 * Description:远程调用sms
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {

}
