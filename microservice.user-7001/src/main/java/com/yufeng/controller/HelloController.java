package com.yufeng.controller;

import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.pojo.Stu;
import com.yufeng.service.StuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 */
@RestController
@RequestMapping("/user")
public class HelloController {

    @Autowired
    private StuService stuService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello, User!";
    }

    @GetMapping("/stu")
    public Object stu() {

        Stu stu = new Stu();
        // stu.setId(1002);
        stu.setName("李四");
        stu.setAge(18);
        stuService.save(stu);

        return "Hello, Stu!";
    }

}
