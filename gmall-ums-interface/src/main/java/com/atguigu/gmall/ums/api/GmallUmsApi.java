package com.atguigu.gmall.ums.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.ums.api.entity.UserEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Date:2021/7/16
 * Author：ZHOU_World
 * Description:ums暴露的接口
 */
public interface GmallUmsApi {
    @GetMapping("ums/user/query")
    public ResponseVo<UserEntity> queryUser(@RequestParam("loginName")String loginName,
                                            @RequestParam("password")String password);
}
