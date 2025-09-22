package com.yufeng;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;


/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:48
 */
@EnableRetry // @EnableRetry 会创建必要的代理对象来拦截带有 @Retryable 注解的方法调用
@MapperScan(basePackages = "com.yufeng.mapper") // 扫描Mapper接口所在的包
@SpringBootApplication
@EnableDiscoveryClient // 开启注册中心的服务注册和发现功能
@EnableFeignClients("com.yufeng.api.feign") // 开启Feign客户端功能，扫描Feign接口所在的包
public class Application {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(Application.class, args);
    }
}
