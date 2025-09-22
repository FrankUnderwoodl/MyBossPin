package com.yufeng.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 */
@RestController
@RequestMapping("/gateway")
@Slf4j
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Gateway!";
    }

    // 通过nacos配置中心来获取JWT的秘钥
    @Value("${jwt.key}")
    private String JWT_KEY;

    @Test
    public void testJWTKey() {
        // 测试获取JWT秘钥
        log.info("从配置中心获取的JWT秘钥为: {}", JWT_KEY);
    }
}
