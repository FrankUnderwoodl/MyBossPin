package com.yufeng.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Lzm
 * @CreateTime 2025年6月16日 01:02
 * @describe 将配置文件里面的内容映射到Java对象中
 */
@Data
@Component // 被Spring容器所扫描到
@PropertySource("classpath:UrlPath.properties") // 指定配置文件所在的位置
@ConfigurationProperties(prefix = "exclude") // 指定配置文件中的前缀
public class ExcludeUrlProperties {

    // 获取不需要jwt验证的路由
    private List<String> urls;
    private String fileRelease;

    // 获取需要保护不被恶意IP攻击的路由
    private List<String> ipLimitUrls;
}
