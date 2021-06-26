package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.SkuSalesVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Date:2021/6/27
 * Author：ZHOU_World
 * Description:远程调用的方法
 */
public interface GmallSmsApi {
    @PostMapping("sms/skubounds/saleSales")
    @ApiOperation("保存积分信息")
    public ResponseVo saleSales(@RequestBody SkuSalesVo saleVo);
}
