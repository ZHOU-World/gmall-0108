package com.atguigu.gmall.common.exception;

/**
 * Date:2021/7/14
 * Author：ZHOU_World
 * Description:详情信息的异常类
 */
public class ItemException extends RuntimeException{
    //重写有参无参构造方法

    public ItemException() {
        super();
    }

    public ItemException(String message) {
        super(message);
    }
}
