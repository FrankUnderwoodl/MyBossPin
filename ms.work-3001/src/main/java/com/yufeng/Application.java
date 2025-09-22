package com.yufeng;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


/**
 * @author Lzm
 * @since 2025年6月03日 00:48
 */
@MapperScan("com.yufeng.mapper") // 扫描Mapper接口所在的包
@EnableMongoRepositories("com.yufeng.repository") // 扫描MongoDB Repository接口所在的包
@SpringBootApplication
@EnableDiscoveryClient // 开启注册中心的服务注册和发现功能
@EnableFeignClients
public class Application {
    public static void main(String[] args) {
        // run方法的作用是启动Spring Boot应用程序
        org.springframework.boot.SpringApplication.run(Application.class, args);
    }
}
