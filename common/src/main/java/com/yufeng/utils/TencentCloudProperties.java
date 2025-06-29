package com.yufeng.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author Lzm
 * @CreateTime 2025年6月16日 01:02
 */
@Data
@Component // 被Spring容器所扫描到
@PropertySource("classpath:tencentCloud.properties") // 指定配置文件位置
@ConfigurationProperties(prefix = "tencent.cloud") // 指定配置文件前缀
public class TencentCloudProperties {

    // @Value("${tencent.cloud.secretId}")
    private String secretId;
    // @Value("${tencent.cloud.secretKey}")
    private String secretKey;
}
