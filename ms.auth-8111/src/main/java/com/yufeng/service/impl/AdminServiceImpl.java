package com.yufeng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yufeng.model.bo.AdminBo;
import com.yufeng.mapper.AdminMapper;
import com.yufeng.model.pojo.Admin;
import com.yufeng.service.AdminService;
import com.yufeng.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 慕聘网运营管理系统的admin账户表，仅登录，不提供注册 服务实现类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    // 注入AdminMapper
    @Autowired
    private AdminMapper adminMapper;


    /**
     * 管理员登录方法
     * @param adminBo 前端传来的登录信息(账号和密码)
     * @return 返回 true表示登录成功，false表示登录失败
     */
    @Override
    public boolean adminLogin(AdminBo adminBo) {

        // 1️⃣尝试通过用户名查询管理员信息
        // Admin admin = adminMapper.selectById(adminBo.getUsername()); // 这种方式不对，因为用户名不是主键
        Admin admin = this.getAdminByUsername(adminBo.getUsername());

        // 2️⃣如果管理员信息不存在，返回登录失败
        if (admin == null) {
            return false;
        } else {
            String slat = admin.getSlat(); // 获取盐
            String md5Str = MD5Utils.encrypt(adminBo.getPassword(), slat);// 盐和密码组合之后，进行MD5加密去跟数据库中的密码进行对比

            // 3️⃣对比撒盐之后的密码和数据库中的密码
            if (md5Str.equalsIgnoreCase(admin.getPassword())) {
                // 4️⃣如果密码匹配，登录成功
                return true;
            }
        }

        // 5️⃣如果密码不匹配，登录失败
        return false;
    }

    /**
     * 获取管理员信息
     * @param adminBo
     * @return
     */
    @Override
    public Admin getAdminInfo(AdminBo adminBo) {
        return getAdminByUsername(adminBo.getUsername());
    }


    /**
     * 通过用户名查询管理员信息(封装成一个私有方法)
     * @param username 管理员用户名
     * @return 返回管理员信息，如果不存在则返回null
     */
    private Admin getAdminByUsername(String username) {
        return adminMapper.selectOne(new QueryWrapper<Admin>().eq("username", username));
    }


}
