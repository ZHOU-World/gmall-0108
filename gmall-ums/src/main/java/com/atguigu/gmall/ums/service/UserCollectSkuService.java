package com.atguigu.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.ums.api.entity.UserCollectSkuEntity;

/**
 * 关注商品表
 *
 * @author ZHOU_World
 * @email 3361949746@qq.com
 * @date 2021-07-16 00:36:22
 */
public interface UserCollectSkuService extends IService<UserCollectSkuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

