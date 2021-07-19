package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.api.entity.UserLoginLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户登陆记录表
 * 
 * @author ZHOU_World
 * @email 3361949746@qq.com
 * @date 2021-07-16 00:36:21
 */
@Mapper
public interface UserLoginLogMapper extends BaseMapper<UserLoginLogEntity> {
	
}
