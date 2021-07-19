package com.atguigu.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.ums.api.entity.UserEntity;

/**
 * 用户表
 *
 * @author ZHOU_World
 * @email 3361949746@qq.com
 * @date 2021-07-16 00:36:21
 */
public interface UserService extends IService<UserEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    //数据校验
    Boolean checkData(String data, Integer type);

    //查询用户
    UserEntity queryUser(String loginName, String password);
}

