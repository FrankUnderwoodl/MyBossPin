package com.yufeng.service;

import com.yufeng.model.bo.AdminBo;
import com.yufeng.model.pojo.Admin;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 慕聘网运营管理系统的admin账户表，仅登录，不提供注册 服务类，也就是不用写注册逻辑
 * 这个接口就是给运营人员使用的，提供登录功能
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
public interface AdminService extends IService<Admin> {


    /**
     * 管理员登录方法
     * @param adminBo 前端传来的登录信息(账号和密码)
     * @return 返回 true表示登录成功，false表示登录失败
     */
    public boolean adminLogin(AdminBo adminBo);


    public Admin getAdminInfo(AdminBo adminBo);
}
