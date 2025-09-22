package com.yufeng.service.impl;

import com.yufeng.pojo.ar.Admin;
import com.yufeng.mapper.AdminMapper;
import com.yufeng.service.AdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 慕聘网运营管理系统的admin账户表，仅登录，不提供注册 服务实现类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-07-14
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

}
