package com.yufeng;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * @author Lzm
 * @since 2025年6月03日 00:48
 */
@MapperScan("com.yufeng.mapper")
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        // run方法的作用是启动Spring Boot应用程序
        org.springframework.boot.SpringApplication.run(Application.class, args);
    }
}
