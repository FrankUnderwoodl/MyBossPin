
//	此资源由 58学课资源站 收集整理
//	想要获取完整课件资料 请访问：58xueke.com
//	百万资源 畅享学习
package com.yufeng.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class OSSConfig {

    @Value("${oss.endpoint}")
    private String endpoint;
    @Value("${oss.fileHost}")
    private String fileHost;
    @Value("${oss.bucketName}")
    private String bucketName;
    @Value("${oss.accessKeyId}")
    private String accessKeyId;
    @Value("${oss.accessKeySecret}")
    private String accessKeySecret;

    @Bean
    public OSSUtils creatOSSClient() {
        return new OSSUtils(endpoint, fileHost, bucketName, accessKeyId, accessKeySecret);
    }
}
