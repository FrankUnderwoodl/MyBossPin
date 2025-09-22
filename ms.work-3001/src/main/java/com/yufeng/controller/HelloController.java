package com.yufeng.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 */
@RestController
@RequestMapping("/work")
public class HelloController {

    @Value("${server.port}") // 默认会从配置文件中读取server.port的值
    private String serverPort;

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Work!";
    }

    @GetMapping("/port")
    public String getPort() {
        return "Work service is running on port: " + serverPort;
    }
}
