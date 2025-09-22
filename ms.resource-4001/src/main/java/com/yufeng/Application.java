package com.yufeng;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:48
 */
@MapperScan(basePackages = "com.yufeng.mapper") // 扫描Mapper接口所在的包
@SpringBootApplication
@EnableDiscoveryClient // 开启注册中心的服务注册和发现功能
public class Application {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(Application.class, args);
    }
}
