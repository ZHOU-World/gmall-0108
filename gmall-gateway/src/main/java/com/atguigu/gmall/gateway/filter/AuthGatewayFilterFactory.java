package com.atguigu.gmall.gateway.filter;

import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.gateway.config.JwtConfigPropertites;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Date:2021/7/17
 * Author：ZHOU_World
 * Description:局部过滤器
 */
@EnableConfigurationProperties(JwtConfigPropertites.class)
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.PathConfig> {
    @Resource//@Autowired
    private JwtConfigPropertites propertites;

    //3、内部实现类
    @Data
    public static class PathConfig{
        //        public String key;
//        public String value;
        //7、实体类中定义集合字段
        private List<String> paths;
    }

    @Override
    public GatewayFilter apply(PathConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                //拦截业务逻辑
                //System.out.println("自定义局部过滤器，只拦截经过特定路由的请求,key="+config.paths);
                //获取请求对象ServerHttpRequest --> HttpServletRequest
                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();

                //获取当前请求路径
                String curPath = request.getURI().getPath();

                //获取拦截路径名单
                List<String> paths = config.paths;
                //1、判断当前请求路径在不在拦截名单中，不在直接放行
                if(paths.stream().allMatch(path->!curPath.startsWith(path))){//不是以拦截名单开头的
                    return chain.filter(exchange);
                }
                
                //2、获取token信息，异步：token头中，同步cookie中
                String token = request.getHeaders().getFirst(propertites.getToken());
                if(StringUtils.isBlank(token)){//查看是否存放在cookie中
                    MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                    HttpCookie cookie = cookies.getFirst(propertites.getCookieName());//获取cookie对象
                    token = cookie.getValue();//获取cookie的值
                }
                //3、判空，token为空，拦截并重定向到登录页面
                if(StringUtils.isBlank(token)){
                    response.setStatusCode(HttpStatus.SEE_OTHER);//设置响应状态码
                    //重定向到什么地方取决于设置的头信息
                    response.getHeaders().set(HttpHeaders.LOCATION,"http:sso.gmall.com/toLogin.html?returnUrl="
                                                +request.getURI());
                    //请求结束,被拦截
                    return  response.setComplete();

                }
                try {
                    //4、解析jwt类型的token,如果解析过程出现异常，则拦截并重定向到登录页面
                    Map<String, Object> map = JwtUtils.getInfoFromToken(token, propertites.getPublicKey());//获取公钥

                    //5、获取载荷中的ip(登录用户)和当前请求ip地址（当前用户是否一致），不一致说明是盗用的，则拦截重定向到登录页面
                    String ip = map.get("ip").toString();//载荷中登录用户的ip地址
                    String curIp = IpUtils.getIpAddressAtGateway(request);//当前用户的IP地址
                    if(!StringUtils.equals(ip,curIp)){
                        response.setStatusCode(HttpStatus.SEE_OTHER);//设置响应状态码
                        //重定向到什么地方取决于设置的头信息
                        response.getHeaders().set(HttpHeaders.LOCATION,"http:sso.gmall.com/toLogin.html?returnUrl="
                                                    +request.getURI());
                        //请求结束,被拦截
                        return  response.setComplete();
                    }
                    //已经解析过jwt,后续的微服务中不用再解析，直接获取即可
                    //6、将解析后的信息传递给后续的微服务--转换对象（通过请求头信息）
                    request.mutate()
                            .header("userId",map.get("userId").toString())
                            .header("username",map.get("username").toString())
                            .build();
                    exchange.mutate().request(request).build();
                    //7、放行
                    return chain.filter(exchange);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatusCode(HttpStatus.SEE_OTHER);//设置响应状态码
                    //重定向到什么地方取决于设置的头信息
                    response.getHeaders().set(HttpHeaders.LOCATION,"http:sso.gmall.com/toLogin.html?returnUrl="
                                                +request.getURI());
                    //请求结束,被拦截
                    return  response.setComplete();
                }

            }
        };
    }
    //5、重写父类无参构造方法
    public AuthGatewayFilterFactory() {
        super(PathConfig.class);
    }

    //6、重写父类方法指定顺序
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("paths");
    }

    //8、一个字段接收多个参数
    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }
}
