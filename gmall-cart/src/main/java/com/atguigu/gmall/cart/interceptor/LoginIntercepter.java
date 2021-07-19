package com.atguigu.gmall.cart.interceptor;

import com.atguigu.gmall.cart.config.JwtConfigPropertites;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
 * Date:2021/7/19
 * Author：ZHOU_World
 * Description:拦截器
 */
public class LoginIntercepter implements HandlerInterceptor {

    @Autowired
    private JwtConfigPropertites propertites;

    //只能传递一个值，需要封装对象UserInfo
    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();//泛型里面是ThreadLocal真正的载荷信息
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                Object handler) throws Exception {
        //获取userKey的内容（存放在cookie中）
        String userKey = CookieUtils.getCookieValue(request, propertites.getUserKey());
        if(StringUtils.isBlank(userKey)){//判空，当userKey为空时，设置值为uuid
            String uuid = UUID.randomUUID().toString();
            CookieUtils.setCookie(request,response,propertites.getUserKey(), uuid,propertites.getExpire());
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setUserKey(userKey);
        //获取userId信息
        String token = CookieUtils.getCookieValue(request,propertites.getCookieName());
        if(StringUtils.isNotBlank(token)){//token不为空
            //解析token
            Map<String, Object> map = JwtUtils.getInfoFromToken(token, propertites.getPublicKey());
            Long userId = (Long)map.get("userId");
            userInfo.setUserId(userId);
        }
        THREAD_LOCAL.set(userInfo);
        //request.setAttribute("userKey",userKey);
        return true;//false拦截，true放行
    }

    //一般情况下，很少将THREAD_LOCAL本身直接给他人，因为可以被别人篡改
    //会提供一个公开的方法，获取载荷（THREAD_LOCAL）的值
    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();//可以获取内容，但不是直接获取
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("后置方法");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        System.out.println("完成方法");
        //由于使用的是tomcat线程池，所有请求结束，线程并没有结束，只是回到了线程池，如果不手动释放资源，会导致内存泄漏
        THREAD_LOCAL.remove();//手动释放资源
    }
}
