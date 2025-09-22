package com.yufeng.service;

import com.yufeng.model.bo.CreateAdminBo;
import com.yufeng.model.bo.UpdateAdminBO;
import com.yufeng.model.pojo.Admin;
import com.yufeng.utils.PagedGridResult;

/**
 * <p>
 * 慕聘网运营管理系统的admin账户表，仅登录，不提供注册 服务类，也就是不用写注册逻辑
 * 这个接口就是给运营人员使用的，提供登录功能
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
public interface AdminService{


    /**
     * 创建一个管理员
     * @param createAdminBo 前端传来的三个参数：用户名、密码、备注信息
     */
    public void createAdmin(CreateAdminBo createAdminBo);



    /**
     * 通过用户名获取管理员信息
     * @return 返回管理员信息
     */
    public PagedGridResult getAdminList(String accountName, Integer page, Integer limit);



    /**
     * 删除管理员
     */
    public void deleteAdmin( String username);


    /**
     * 查询管理员信息
     */
    public Admin getById(String adminId);


    /**
     * 更新管理员信息
     * @param adminBo 管理员信息的业务对象
     */
    public void updateAdmin(UpdateAdminBO adminBo);

}
