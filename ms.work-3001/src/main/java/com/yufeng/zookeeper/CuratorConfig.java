package com.yufeng.zookeeper;// package com.yufeng.api.zookeeper;

import com.github.benmanes.caffeine.cache.Cache;
import com.yufeng.base.BaseInfoProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author Lzm
 * @CreateTime 2025年9月16日 18:53
 * 这个类的作用其实类似于Canal，通过监听zookeeper中的节点，来实现分布式缓存的动态更新
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "zookeeper.curator")
public class CuratorConfig extends BaseInfoProperties {

    private String host;                    // 单机/集群的ip:port地址
    private Integer connectionTimeoutMs;    // 连接超时时间
    private Integer sessionTimeoutMs;         // 会话超时时间
    private Integer sleepMsBetweenRetry;    // 每次重试的间隔时间
    private Integer maxRetries;             // 最大重试次数
    private String namespace;               // 命名空间（root根节点名称）


    public static final String PATH = "/" + ZK_MAX_RESUME_REFRESH_COUNTS;


    @Autowired
    private Cache<String, Integer> cache;


    @Bean("curatorClient")
    public CuratorFramework curatorClient() {
        // 三秒后重连一次，只连一次
        //RetryPolicy retryOneTime = new RetryOneTime(3000);
        // 每3秒重连一次，重连3次
        //RetryPolicy retryNTimes = new RetryNTimes(3, 3000);
        // 每3秒重连一次，总等待时间超过10秒则停止重连
        //RetryPolicy retryPolicy = new RetryUntilElapsed(10 * 1000, 3000);
        // 随着重试次数的增加，重试的间隔时间也会增加（推荐）
        RetryPolicy backoffRetry = new ExponentialBackoffRetry(sleepMsBetweenRetry, maxRetries);

        // 声明初始化客户端
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(host)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .retryPolicy(backoffRetry)
                .namespace(namespace)
                .build();
        client.start();     // 启动curator客户端
        // 来到这里并不会马上创建一个命名空间，因为curator是懒加载的，只有当你创建节点的时候才会创建命名空间

        // 注册事件
        add(PATH, client);
        return client;
    }

    /**
     * 注册节点的事件监听(不单是监听当前路径的数据，还监听子节点的数据)
     */
    public void add(String path, CuratorFramework client) {

        // 创建一个监听器
        CuratorCache curatorCache = CuratorCache.build(client, path);
        // 注册监听器 type: 当前监听到的事件类型    oldData: 节点更新前的数据、状态    data: 节点更新后的数据、状态
        // 三种事件类型：NODE_CREATED、NODE_CHANGED、NODE_DELETED
        curatorCache.listenable().addListener((type, oldData, data) -> {

            // 监听子节点的变更
            switch (type.name()) {
                case "NODE_CREATED":
                    log.info("(子)节点创建");
                    break;
                case "NODE_CHANGED":
                    log.info("(子)节点数据变更，oldData.getPath() = {}", oldData.getPath());

                    if (oldData.getPath().equals(path)) {
                        log.info("监听到{}的数据发生变更...", path);

                        Integer oldMaxCounts = (Integer)this.cache.asMap().get(CACHE_MAX_RESUME_REFRESH_COUNTS);
                        log.info("原来的oldMaxCounts = {}", oldMaxCounts);

                        // 得到新的节点数据
                        Integer newMaxCounts = Integer.valueOf(new String(data.getData()));
                        // 各个微服务节点监听到变动，则更新各自的本地Caffeine缓存
                        cache.put(CACHE_MAX_RESUME_REFRESH_COUNTS, newMaxCounts);

                        log.info("简历微服务节点本地缓存已更新...更新后的[最大刷新阈值]为：{}", newMaxCounts);
                    }

                    break;
                case "NODE_DELETED":
                    log.info("(子)节点删除");
                    break;
                default:
                    break;
            }

        });

        curatorCache.start();
    }
}
