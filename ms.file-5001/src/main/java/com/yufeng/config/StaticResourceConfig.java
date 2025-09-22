package com.yufeng.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @author Lzm
 * @CreateTime 2025年8月03日 06:00
 * 本质跟拦截器配置类一样，都是继承WebMvcConfigurationSupport
 */
@Configuration
public class StaticResourceConfig extends WebMvcConfigurationSupport {



    /**
     * 添加静态资源映射的路径，css/js/html等都可以放在其中进行虚拟化
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {

        /**
         * addResourceHandler: 指的对外暴露的访问路径
         * addResourceLocations: 指的是文件配置的本地系统目录，
         * 举个例子:
         * /static/css/style.css
         * /Users/lzm/Downloads/temp/css/style.css
         * ------------------------------------------
         * /static/js/app.js
         * /Users/lzm/Downloads/temp/js/app.js
         * ------------------------------------------
         * /static/images/logo.png
         * /Users/lzm/Downloads/temp/images/logo.png
         */
        registry.addResourceHandler("/static/**")
                .addResourceLocations("file:/Users/lzm/Downloads/temp/");

        super.addResourceHandlers(registry);
    }
}
