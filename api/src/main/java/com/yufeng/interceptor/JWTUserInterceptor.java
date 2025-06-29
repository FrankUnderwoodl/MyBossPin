package com.yufeng.interceptor;

import com.google.gson.Gson;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.exception.GraceException;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.pojo.Admin;
import com.yufeng.pojo.Users;
import com.yufeng.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Lzm
 * @CreateTime 2025年6月22日23:55:36
 */
@Slf4j
public class JWTUserInterceptor extends BaseInfoProperties implements HandlerInterceptor {

    /**
     * 问：为什么需要ThreadLocal？
     * 答：因为Controller、Service、Mapper本质上都是在同一个线程中运行的，所以可以使用ThreadLocal来存储用户信息
     */

    // 这里的ThreadLocal底层其实是一个Entry
    public static ThreadLocal<Users> currentUser = new ThreadLocal<>();
    public static ThreadLocal<Admin> adminUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String appUserJson = request.getHeader(APP_USER_JSON); // 普通用户
        String saasUserJson = request.getHeader(SAAS_USER_JSON); // SAAS用户
        String adminUserJson = request.getHeader(ADMIN_USER_JSON); // 管理员用户

        // 对json进行判定
        if (StringUtils.isNotBlank(appUserJson) || StringUtils.isNotBlank(saasUserJson)) {
            // 如果appUserJson不为空，则说明是普通用户或者SAAS用户
            Users users = new Gson().fromJson(appUserJson, Users.class);
            // 设置当前用户到ThreadLocal中
            currentUser.set(users);
            log.info("从请求头中获取到的用户信息为: {}", users);
        }

        if (StringUtils.isNotBlank(adminUserJson)) {
            // 如果adminUserJson不为空，则说明是管理员用户
            Admin admin = new Gson().fromJson(adminUserJson, Admin.class);
            adminUser.set(admin);
            log.info("从请求头中获取到的管理员信息为: {}", admin);
        }
        return true;
    }

    /**
     * 在请求处理完成(前端接收完数据后)执行的回调方法
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        // 清除ThreadLocal中的用户信息(因为当前线程可能会被复用，所以需要在每次请求结束后马上清除)
        currentUser.remove();
        adminUser.remove();
    }
}















