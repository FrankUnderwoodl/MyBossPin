package com.yufeng.service;

import com.yufeng.model.pojo.Users;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
public interface UsersService extends IService<Users> {

    /**
     * 通过手机号判断用户是否存在，如果存在，则返回用户信息，否则null
     */
    public Users queryUserIsExist(String mobile);

    /**
     * 创建一个新用户
     */
    public Users createUser(String mobile);
}
