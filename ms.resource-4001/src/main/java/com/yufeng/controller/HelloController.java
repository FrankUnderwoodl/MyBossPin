package com.yufeng.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 */
@RestController
@RequestMapping("/resource")
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Resource Service!";
    }
}
