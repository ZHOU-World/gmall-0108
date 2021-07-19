package com.atguigu.gmall.auth.config;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
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
    private String priFilePath;
    private String secret;
    private Integer expire;
    private String cookieName;
    private String unick;

    private PublicKey publicKey;

    private PrivateKey privateKey;
    @PostConstruct //启动时获取公钥私钥
    public void init(){
        try {
            File pubFile = new File(pubFilePath);
            File priFile = new File(priFilePath);
            //判断
            if(!pubFile.exists()||!priFile.exists()){
                RsaUtils.generateKey(pubFilePath,priFilePath,secret);
            }
            this.privateKey = RsaUtils.getPrivateKey(priFilePath);
            this.publicKey = RsaUtils.getPublicKey(pubFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
