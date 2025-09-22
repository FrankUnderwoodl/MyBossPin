package com.yufeng.controller;

import com.yufeng.base.BaseInfoProperties;
import com.yufeng.api.interceptor.JWTUserInterceptor;
import com.yufeng.model.pojo.Stu;
import com.yufeng.model.pojo.Users;
import com.yufeng.service.StuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class HelloCtl extends BaseInfoProperties {

    @Autowired
    private StuService stuService;

    @Value("${server.port}")
    private String port;

    @GetMapping("/hello")
    public String hello(HttpServletRequest request) {

        // 通过request对象获取用户信息(打破传统的要访问数据才能获取用户信息的方式，直接就是从请求头中获取)
       /*  String userJson = request.getHeader(APP_USER_JSON); // 通过键值对来获取请求头中的用户信息
        if (StringUtils.isNotBlank(userJson)) {
            Users users = new Gson().fromJson(userJson, Users.class);
            log.info("从Request请求头中，获取到的用户信息为: {}", users);
        } */

        // 这里可以直接从ThreadLocal中获取用户信息，因为api层的Interceptor已经将用户信息存储到ThreadLocal中了
        Users currentUser = JWTUserInterceptor.currentUser.get();
        // Admin admin = JWTUserInterceptor.adminUser.get();
        log.info("从ThreadLocal中获取到的用户信息为: {}", currentUser.toString());
        // log.info("从ThreadLocal中获取到的管理员用户信息为: {}", admin.toString());

        return "Hello, User!";
    }

    @GetMapping("/stu")
    public Object stu() {
        // 测试MyBatis-Plus的自动生成主键功能
        Stu stu = new Stu();
        // 这里不需要设置ID，因为MyBatis-Plus会自动生成主键
        // stu.setId(1002);
        stu.setName("王五");
        stu.setAge(18);
        stuService.save(stu);

        log.info("lb测试，端口号为：{}", port);
        return "Hello, Stu!";
    }

}
