package com.atguigu.gmall.cart.config;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

/**
 * Date:2021/7/17
 * Author：ZHOU_World
 * Description:
 */
@Data
@ConfigurationProperties(prefix = "jwt") //需要找到详情的类进行启用，即不报错
public class JwtConfigPropertites {
    private String pubFilePath;
    private String cookieName;
    private String userKey;
    private Integer expire;

    private PublicKey publicKey;

    @PostConstruct //启动时获取公钥私钥
    public void init(){
        try {

            this.publicKey = RsaUtils.getPublicKey(pubFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
