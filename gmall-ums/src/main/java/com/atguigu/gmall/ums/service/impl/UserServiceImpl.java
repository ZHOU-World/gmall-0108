package com.atguigu.gmall.ums.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.api.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Resource//@Autowired
    private UserMapper userMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );
        return new PageResultVo(page);
    }

    //数据校验
    @Override
    public Boolean checkData(String data, Integer type) {
        //使用list,getOne，selectOne，count都可以
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        switch (type) {
            case 1:
                wrapper.eq("username", data);
                break;
            case 2:
                wrapper.eq("phone", data);
                break;
            case 3:
                wrapper.eq("email", data);
                break;
            default:
                return null;
        }
        return this.count(wrapper) == 0;
    }

    //查询用户
    @Override
    public UserEntity queryUser(String loginName, String password) {
        //1、根据登录名查询用户信息(可以通过userName，phone,email进其中之一进行查询)
        List<UserEntity> userEntityList = this.list(new QueryWrapper<UserEntity>()
                .eq("username", loginName)
                .or().eq("phone", loginName)
                .or().eq("email", loginName));
        //2、判空，如果用户为空，直接返回，不处理
        if (CollectionUtils.isEmpty(userEntityList)) {
            return null;
        }
        //3、获取盐，对用户输入的明文密码进行加密
        for (UserEntity userEntity : userEntityList) {
            //加密加盐后的密码与数据的密码进行比较
            if (StringUtils.equals(DigestUtils.md5Hex(password + userEntity.getSalt()), userEntity.getPassword())) {
                return userEntity;
            }
        }
        return null;
    }
}

