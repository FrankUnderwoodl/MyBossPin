package com.yufeng;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import javax.sql.DataSource;


/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:48
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})  // 排除数据源自动配置(你可以把SpringBoot想象成一个清单，里面列出了所有的依赖和配置，现在我们就把数据库等相关的依赖和配置从清单中划掉)
@EnableDiscoveryClient // 开启注册中心的服务注册和发现功能
public class Application {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(Application.class, args);
    }
}
