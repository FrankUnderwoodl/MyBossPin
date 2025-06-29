package com.yufeng.config;

import com.yufeng.interceptor.JWTUserInterceptor;
import com.yufeng.interceptor.SMSInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Lzm
 * @CreateTime 2025年6月16日 06:08
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    // 将拦截器作为一个Bean注册到Spring容器中
    @Bean
    public SMSInterceptor smsInterceptor() {
        return new SMSInterceptor();
    }

    @Bean
    public JWTUserInterceptor jwtInterceptor() {
        return  new JWTUserInterceptor();
    }

    // 注册拦截器，并且拦截指定的路径
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 防止用户在注册时，频繁获取短信验证码
        registry.addInterceptor(smsInterceptor()).addPathPatterns("/passport/getSMSCode");

        // 让每个请求，不用往service、mapper层传递用户信息，直接就能通过ThreadLocal获取到用户信息
        registry.addInterceptor(jwtInterceptor()).addPathPatterns("/**");
    }
}
