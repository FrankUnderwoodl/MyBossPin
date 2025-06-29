package com.yufeng;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author Lzm
 * @CreateTime 2025年6月24日 04:04
 *
 */
// @Configuration
public class CorsConfig { // Cross Origin Resource Sharing

    @Bean
    public CorsWebFilter corsFilter() {

        // 1.添加cors配置相关信息
        CorsConfiguration config = new CorsConfiguration();
        // 允许跨域的域名，可以用*表示允许所有域名
        config.addAllowedOriginPattern("*"); // 允许所有域名访问，*表示允许所有域名
        // 设置是否允许携带cookie
        config.setAllowCredentials(true); // 是否允许携带cookie
        // 设置允许的请求方法
        config.addAllowedMethod("*"); // 允许的请求方法，*表示允许所有方法
        // 设置允许的请求头
        config.addAllowedHeader("*"); // 允许的请求头，*表示允许所有请求头

        // 2.添加映射路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 允许所有路径的跨域请求

        // 3.返回新的CorsWebFilter
        return new CorsWebFilter(source);
    }
}
