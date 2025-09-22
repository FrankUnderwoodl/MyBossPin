package com.yufeng.api.bean_config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Lzm
 * @CreateTime 2025年8月26日 11:26
 */
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {

        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://localhost:6379")
                .setDatabase(0)                        // 指定16个库中可以设置某个下标的库
                .setConnectionMinimumIdleSize(10)      // 连接池中始终保持的最少空闲连接数，即使没有请求，也会维持这些连接不被关闭，超过上限的请求需要等待连接释放
                .setConnectionPoolSize(20)             // 连接池能够创建的最大连接数上限，当并发请求超过空闲连接时，会创建新连接直到达到这个上限，超过上限的请求需要等待连接释放
                .setIdleConnectionTimeout(60 * 1000)   // 销毁超时的时间，这里是60秒，表示如果一个连接在连接池中空闲时间超过60秒，就会被销毁
                .setConnectTimeout(15 * 1000)          // 客户端尝试连接到Redis服务器的超时时间，这里是15秒，如果在这个时间内无法建立连接，连接尝试将被放弃
                .setTimeout(15 * 1000);                // 等待Redis服务器响应的超时时间，这里是15秒，如果在这个时间内没有收到响应，操作将被放弃


        return Redisson.create(config);
    }
}
