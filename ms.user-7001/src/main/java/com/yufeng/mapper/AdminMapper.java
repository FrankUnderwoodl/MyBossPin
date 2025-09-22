package com.yufeng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yufeng.model.pojo.Admin;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 慕聘网运营管理系统的admin账户表，仅登录，不提供注册 Mapper 接口
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Repository // 标记为Spring的Repository组件，也就是交给Spring管理
public interface AdminMapper extends BaseMapper<Admin> {

}
