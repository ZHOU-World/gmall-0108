package com.atguigu.gmall.auth;


import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * Date:2021/7/16
 * Author：ZHOU_World
 * Description:
 */
public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
    private static final String pubKeyPath = "D:\\project-0108\\rsa\\rsa.pub";
    private static final String priKeyPath = "D:\\project-0108\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

        @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2MjY0NDEzOTJ9.C-O5vlz3q_fGAelJ1zo0XQoEIdJxr--8cwhAY1JC9qYO1AXoSH1uvnx90KpOE7yprpHWm0SF9r5wPavPoeSkcGEKNGF8_DyGC3vr3AT4raSHzdtesNOd6DXQ8JfmFCPNr6EiAyMKnfSTwT14viDl8qQkd8U8JxgV0CYww_bCnlAzayvJzNvcN-Lnl6P76bubOTqGTMXJRB5ESpyQFH05O555dvkNV34__xv9XTJ8cY6X3dePmkIb48rmpRch0z7RwY0XVcIyYDXHfwRtmFYtcMi0AJ0d4-CMnS8kJz5-U6iudj3B2wkm9vdVJnMXVV6_tNT2UCRsTPvKA-ZSFocoVw\n";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}


