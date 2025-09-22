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
@Component // 放入IOC容器中
@PropertySource("classpath:UrlPath.properties") // 指定配置文件所在的位置
@ConfigurationProperties(prefix = "include") // 指定配置文件中的前缀
public class IncludeUrlProperties {

    // 获取需要保护不被恶意IP攻击的路由
    private List<String> ipLimitUrls;
}
