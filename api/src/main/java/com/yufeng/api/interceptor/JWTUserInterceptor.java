package com.yufeng.api.interceptor;

import com.google.gson.Gson;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.model.pojo.Admin;
import com.yufeng.model.pojo.Users;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Lzm
 * @CreateTime 2025年6月22日23:55:36
 * @describe JWT用户拦截器，将用户信息存储到ThreadLocal中，方便在Controller、Service、Mapper等层中直接获取用户信息
 */
@Slf4j
public class JWTUserInterceptor extends BaseInfoProperties implements HandlerInterceptor {

    /**
     * 问：为什么需要ThreadLocal？
     * 答：因为Controller、Service、Mapper本质上都是在同一个线程中运行的，所以可以使用ThreadLocal来存储用户信息
     * 问：为什么这里需要static修饰呀？
     * 答：因为ThreadLocal是一个线程局部变量，它的作用域是当前线程，所以需要使用static修饰，可以保证在同一个线程中，所有的请求都能获取到同一个用户信息
     */
    public static ThreadLocal<Users> currentUser = new ThreadLocal<>();
    public static ThreadLocal<Admin> adminUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1️⃣在请求头中，根据key，获取value
        String appUserJson = request.getHeader(APP_USER_JSON);     // 普通用户
        String saasUserJson = request.getHeader(SAAS_USER_JSON);   // SAAS用户
        String adminUserJson = request.getHeader(ADMIN_USER_JSON); // 管理员用户


        // 2️⃣对value进行判定，判断是哪种用户
        // SAAS用户和普通用户其实本质都是Users对象，所以可以将它们合并处理
        if (StringUtils.isNotBlank(appUserJson) || StringUtils.isNotBlank(saasUserJson)) {

            // 根据实际的用户类型选择对应的JSON字符串进行解析
            // 错误写法：Users users = new Gson().fromJson(appUserJson, Users.class);
            String userJson = StringUtils.isNotBlank(appUserJson) ? appUserJson : saasUserJson;
            Users users = new Gson().fromJson(userJson, Users.class);
            // 设置当前用户到ThreadLocal中
            currentUser.set(users);
            // log.info("从请求头中获取到的用户信息为: {}", users);
            // log.info("有移动端、Sass端的用户请求，该用户名字为: {}", users.getNickname());
        }

        // 如果appUserJson和saasUserJson都为空，则说明是管理员用户
        if (StringUtils.isNotBlank(adminUserJson)) {
            // 如果adminUserJson不为空，则说明是管理员用户
            Admin admin = new Gson().fromJson(adminUserJson, Admin.class);
            adminUser.set(admin);
            // log.info("从请求头中获取到的管理员信息为: {}", admin);
            log.info("有管理端的用户请求，该管理员名字为: {}", admin.getUsername());
        }
        return true;
    }




    /**
     * 在请求处理完成(前端接收完数据后)执行的回调方法
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        // 清除ThreadLocal中的用户信息
        currentUser.remove();
        adminUser.remove();
    }
}















