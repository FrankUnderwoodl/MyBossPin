package com.yufeng.filter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Lzm
 * @CreateTime 2025年6月16日 01:02
 */
@Data
@Component // 被Spring容器所扫描到
@PropertySource("classpath:excludeUrlPath.properties") // 指定配置文件所在的位置
@ConfigurationProperties(prefix = "exclude") // 指定配置文件中的前缀
public class ExcludeUrlProperties {

    // 排除路径的数据
    private List<String> urls;
}
