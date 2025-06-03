package com.yufeng.service.impl;

import com.yufeng.pojo.Users;
import com.yufeng.mapper.UsersMapper;
import com.yufeng.service.UsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-03
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

}
