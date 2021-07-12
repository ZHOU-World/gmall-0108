package com.atguigu.gmall.index;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Date:2021/7/12
 * Author：ZHOU_World
 * Description:
 */
public class JDKProxy implements InvocationHandler {

    private Object target;

    public JDKProxy(Object target){
        this.target = target;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //增强
        Object result = method.invoke(args);
        //增强
        return result;
    }

    public Object newInstance(){
        return Proxy.newProxyInstance(target.getClass().getClassLoader(),target.getClass().getInterfaces(),this);
    }
}

/**public Object newInstance(Object target){
 ClassLoader classLoader = target.getClass().getClassLoader();
 Class<?>[] interfaces = target.getClass().getInterfaces();
 return Proxy.newProxyInstance(classLoader, interfaces, new InvocationHandler() {
@Override
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//增强
Object result = null;//调用目标方法执行核心业务（加减乘除）
try {
result = method.invoke(target, args);
} catch (Exception e) {
//异常信息
}
//增强方法
return result;
}
});
 }*/