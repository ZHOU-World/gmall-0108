package com.atguigu.gmall.auth.service;

import com.atguigu.gmall.auth.config.JwtConfigPropertites;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.AuthException;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.api.entity.UserEntity;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * Date:2021/7/16
 * Author：ZHOU_World
 * Description:
 */
@EnableConfigurationProperties(JwtConfigPropertites.class)
@Service
public class AuthService {
    //登录
    @Resource//@Autowired
    private GmallUmsClient gmallUmsClient;

    @Resource//@Autowired
    private JwtConfigPropertites propertites;

    public void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //1、远程调用接口，验证用户的登录名和密码（查询）
        ResponseVo<UserEntity> userEntityResponseVo = this.gmallUmsClient.queryUser(loginName, password);
        UserEntity userEntity = userEntityResponseVo.getData();
        //2、判空
        if(userEntity==null){
            throw new AuthException("登录名或密码错误，请重新输入");
        }
        //3、组装载荷
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId",userEntity.getId());
        map.put("password", userEntity.getPassword());
        //4、防止jwt盗用，加入登录的ip地址
        String ip = IpUtils.getIpAddressAtService(request);
        map.put("ip",ip);//放入载荷中
        //5、生成jwt
        //公钥私钥授权中心生成（运维人为介入）
        String token = JwtUtils.generateToken(map, this.propertites.getPrivateKey(), this.propertites.getExpire());
        //6、把jwt设置到cookie中（通过cookieUtils）  //过期时间与JWT过期时间一致即可
        CookieUtils.setCookie(request,response,this.propertites.getCookieName(),
                                token,this.propertites.getExpire()*60);
        //7、把unick放入cookie中
        CookieUtils.setCookie(request,response,this.propertites.getUnick(),
                                userEntity.getNickname(),this.propertites.getExpire()*60);
    }
}
