package com.yufeng.api.bean_config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Lzm
 * @CreateTime 2025年6月16日 06:08
 * @describe 配置一个Caffeine缓存，其实本质就是一个ConcurrentHashMap，但是caffeine底层是基于LRU算法(最近最少使用算法)来进行缓存淘汰
 */
@Configuration
public class CaffeineConfig implements WebMvcConfigurer {


    /**
     * 用来配置Caffeine缓存，所有的微服务都可以使用这个缓存Bean
     *
     * @return a Cache object configured with Caffeine
     */
    @Bean
    public Cache<String, Object> cache() {
        return Caffeine.newBuilder()
                .initialCapacity(50) // 初始的容量，这里是50(可以避免动态扩容带来的性能损耗)
                .maximumSize(1000) // 最大能有几个key
                .build();
    }


    /**
     * 专门用于设置[简历刷新次数]的缓存
     *
     * @return a Cache object configured with Caffeine for resume refresh counts
     */
    @Bean
    public Cache<String, Integer> resumeRefreshCountsCache() {
        return Caffeine.newBuilder()
                //.initialCapacity(1)
                .maximumSize(1)
                .build();
    }

}
