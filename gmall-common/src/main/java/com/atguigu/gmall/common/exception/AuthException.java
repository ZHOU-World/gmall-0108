package com.atguigu.gmall.common.exception;

/**
 * Date:2021/7/17
 * Author：ZHOU_World
 * Description:授权管理自定义异常
 */
public class AuthException extends RuntimeException{
    public AuthException() {
    }

    public AuthException(String message) {
        super(message);
    }
}
