package jvm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Main {

    public static void main(String[] args) {
        try {
            String classPath = System.getProperty("user.dir")+"/src/jvm/Hello.xlass";
            // 创建自定义类加载器
            HelloClassLoader helloClassLoader = new HelloClassLoader(classPath);
            // 加载类
            Class<?> Hello = helloClassLoader.findClass("Hello");
            //利用反射获取hello方法
            Method method = Hello.getDeclaredMethod("hello");
            Object object = Hello.newInstance();
            method.invoke(object);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }


}


