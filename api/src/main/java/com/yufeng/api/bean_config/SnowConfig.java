package com.yufeng.api.bean_config;

import com.a3test.component.idworker.IdWorkerConfigBean;
import com.a3test.component.idworker.Snowflake;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Lzm
 * @CreateTime 2025年6月16日 06:08
 * @describe 配置一个雪花算法的Bean
 */
@Configuration
public class SnowConfig implements WebMvcConfigurer {


    /**
     * 声明雪花算法的Bean(因为容器里只扫描了com.yufeng下的所有类，而这个组件在com.a3test下，所以需要手动配置一个Bean)
     *
     * @return Snowflake
     */
    @Bean
    public Snowflake snowflake() {
        return new Snowflake(new IdWorkerConfigBean());
    }

}
