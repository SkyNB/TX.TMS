package com.lnet.tms.publisher;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * Created by develop on 2016/12/27.
 */
public class Startup {
    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:publisher.xml");
        applicationContext.start();

        System.out.println("tms server started...");

        System.in.read();//按任意键退出
    }
}
