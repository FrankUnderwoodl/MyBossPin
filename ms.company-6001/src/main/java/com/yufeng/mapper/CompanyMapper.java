package com.yufeng.mapper;

import com.yufeng.model.pojo.Company;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 企业表 Mapper 接口
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Repository // 有了这个注解，mybatis才能帮你自动代理成一个Bean
public interface CompanyMapper extends BaseMapper<Company> {

}
