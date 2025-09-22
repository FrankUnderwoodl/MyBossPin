package com.yufeng.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author Lzm
 * @CreateTime 2025年6月16日 01:02
 */
@Data
@Component // 被Spring容器所扫描到
@PropertySource("classpath:jwt.properties") // 指定配置文件所在的位置
@ConfigurationProperties(prefix = "auth") // 指定配置文件中的前缀
public class JWTProperties {
    // JWT的密钥
    private String key;
}
