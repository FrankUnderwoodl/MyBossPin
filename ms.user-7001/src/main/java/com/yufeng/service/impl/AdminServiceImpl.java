package com.yufeng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.model.bo.CreateAdminBo;
import com.yufeng.model.bo.UpdateAdminBO;
import com.yufeng.exception.GraceException;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.mapper.AdminMapper;
import com.yufeng.model.pojo.Admin;
import com.yufeng.service.AdminService;
import com.yufeng.utils.MD5Utils;
import com.yufeng.utils.PagedGridResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 慕聘网运营管理系统的admin账户表，仅登录，不提供注册 服务实现类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Slf4j
@Service
public class AdminServiceImpl extends BaseInfoProperties implements AdminService {

    // 注入AdminMapper
    @Autowired
    private AdminMapper adminMapper;


    /**
     * 创建一个管理员
     *
     * @param createAdminBo 前端传来的三个参数：用户名、密码、备注信息
     */
    @Override
    @Transactional
    public void createAdmin(CreateAdminBo createAdminBo) {

        // 1️⃣首先通过用户名判断账号是否存在
        Admin existingAdmin = getAdminByUsername(createAdminBo.getUsername());
        // 优雅异常的完美体现，如果不这么做，就需要给controller返回一个boolean，然后controller又需要判断等操作
        if (existingAdmin != null) { // 如果用户已经他妈的存在了，则抛出异常
            GraceException.doException(ResponseStatusEnum.ADMIN_USERNAME_EXIST_ERROR);
        }

        // 2️⃣如果不存在，则创建一个新的管理员
        Admin newAdmin = new Admin();
        BeanUtils.copyProperties(createAdminBo, newAdmin);
        // 生成随机数字的盐值
        String salt = MD5Utils.getRandomSalt();
        // 对密码进行加密
        String encryptedPassword = MD5Utils.encrypt(createAdminBo.getPassword(), salt);
        // 设置加密后的密码和盐值
        newAdmin.setPassword(encryptedPassword);
        newAdmin.setSlat(salt);
        // 设置创建时间和更新时间
        newAdmin.setCreateTime(java.time.LocalDateTime.now());
        newAdmin.setUpdatedTime(java.time.LocalDateTime.now());

        // 3️⃣调用Mapper的insert方法将新管理员信息插入到数据库中
        adminMapper.insert(newAdmin);

    }


    /**
     * 获取管理员列表，支持分页和模糊查询。
     *
     * @param accountName 管理员用户名（用于模糊查询）
     * @param page        当前页码
     * @param limit       每页显示的记录数
     * @return 返回包含分页信息和管理员列表的PagedGridResult对象
     */
    @Override
    public PagedGridResult getAdminList(String accountName, Integer page, Integer limit) {

        /*
          PageHelper 的工作原理：
           ①拦截器机制：PageHelper 实现了 MyBatis 的 Interceptor 接口，在 SQL 执行前进行拦截(类似于BeanPostProcessor，也就是实例完一个Bean后，进行一些额外的操作)
           ②ThreadLocal 存储：PageHelper.startPage(page, limit) 将分页参数存储到 ThreadLocal 中
           ③SQL 改写：当执行 adminMapper.selectList() 时，拦截器检测到 ThreadLocal 中有分页参数，自动在原始 SQL 后面追加 LIMIT 子句
           ④自动清理：SQL 执行完成后，自动清理 ThreadLocal 中的分页参数

           举个例子：
              假设你调用了 PageHelper.startPage(1, 10)，然后执行了一个查询语句，PageHelper 会在这个查询语句的 SQL 后面自动加上 LIMIT 0, 10，
         */
        PageHelper.startPage(page, limit);

        // SELECT * FROM admin WHERE username = 'accountName的值'
        List<Admin> adminList = adminMapper.selectList(
                // new QueryWrapper<Admin>().eq("username", accountName) // 你傻呀，如果用等值查询，就只能返回一个结果了
                new QueryWrapper<Admin>().like("username", accountName) // 使用模糊查询，
        );

        return setterPagedGrid(adminList, page); // 将查询结果封装成PagedGridResult对象并返回
    }


    @Override
    @Transactional
    public void deleteAdmin(String username) {
        // log.info("delete admin by username {}", username);

        // 1️⃣首先通过用户名查询管理员信息
        Admin admin = getAdminByUsername(username);

        // 2️⃣如果不存在并且用户名不等于"admin"，则抛出异常
        if (admin == null && admin.getUsername() != "admin") { // 不能删除超级管理员
            GraceException.doException(ResponseStatusEnum.ADMIN_DELETE_ERROR);
        }

        // 3️⃣如果存在，则调用Mapper的delete方法删除管理员信息
        // adminMapper.delete(new QueryWrapper<Admin>().eq("username", username));
        adminMapper.deleteById(admin.getId()); // 因为主键有索引，所以直接通过主键删除会更高效
    }


    @Override
    public Admin getById(String adminId) {
        return adminMapper.selectById(adminId);
    }


    /**
     * 更新管理员信息
     *
     * @param adminBo 管理员信息的业务对象
     */
    @Override
    @Transactional
    public void updateAdmin(UpdateAdminBO adminBo) {

        // 1️⃣创建一个Admin对象，并将adminBo的属性复制到Admin对象中
        Admin admin = new Admin();
        BeanUtils.copyProperties(adminBo, admin);
        // 2️⃣设置更新时间
        admin.setUpdatedTime(java.time.LocalDateTime.now());
        // 3️⃣调用Mapper的updateById方法更新管理员信息
        adminMapper.updateById(admin);
    }


    /**
     * 通过用户名查询管理员信息(封装成一个私有方法)
     *
     * @param username 管理员用户名
     * @return 返回管理员信息，如果不存在则返回null
     */
    private Admin getAdminByUsername(String username) {
        return adminMapper.selectOne(new QueryWrapper<Admin>().eq("username", username));
    }
}
