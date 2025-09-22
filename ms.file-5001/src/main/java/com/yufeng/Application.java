package com.yufeng;

import org.apache.seata.spring.boot.autoconfigure.SeataAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:48
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, SeataAutoConfiguration.class})
@EnableDiscoveryClient // 开启注册中心的服务注册和发现功能
public class Application {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(Application.class, args);
    }
}
