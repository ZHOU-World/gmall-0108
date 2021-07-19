package com.atguigu.gmall.ums.controller;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.ums.api.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 用户表
 *
 * @author ZHOU_World
 * @email 3361949746@qq.com
 * @date 2021-07-16 00:36:21
 */
@Api(tags = "用户表 管理")
@RestController
@RequestMapping("ums/user")
public class UserController {
    @Autowired
    private UserService userService;

    //查询用户
    @GetMapping("query")
    public ResponseVo<UserEntity> queryUser(@RequestParam("loginName")String loginName,
                                            @RequestParam("password")String password){
        UserEntity userEntity = this.userService.queryUser(loginName,password);
        return ResponseVo.ok(userEntity);
    }

    //注册用户
    @PostMapping("register")
    public ResponseVo regist(UserEntity userEntity,String code){
        //todo:验证验证码是否正确
        //生成盐（加密）
        String salt = StringUtils.substring(UUID.randomUUID().toString(), 0, 6);
        userEntity.setSalt(salt);
        //对密码加盐加密(apache提供的加密类)
        String md5Hex = DigestUtils.md5Hex(userEntity.getPassword() + salt);
        userEntity.setPassword(md5Hex);

        //先设置默认值
        userEntity.setLevelId(1L);
        userEntity.setNickname(userEntity.getUsername());
        userEntity.setSourceType(1);
        userEntity.setIntegration(1000);
        userEntity.setGrowth(1000);
        userEntity.setStatus(1);
        userEntity.setCreateTime(new Date());
        //新增用户
        this.save(userEntity);
        //todo:删除redis中的验证码
        return ResponseVo.ok();
    }

    //数据校验
    @GetMapping("check/{data}/{type}")
    public ResponseVo<Boolean> checkData(@PathVariable("data")String data,
                                         @PathVariable("type")Integer type){
        Boolean flag = this.userService.checkData(data,type);
        return ResponseVo.ok(flag);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryUserByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = userService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<UserEntity> queryUserById(@PathVariable("id") Long id){
		UserEntity user = userService.getById(id);

        return ResponseVo.ok(user);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody UserEntity user){
		userService.save(user);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody UserEntity user){
		userService.updateById(user);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		userService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
