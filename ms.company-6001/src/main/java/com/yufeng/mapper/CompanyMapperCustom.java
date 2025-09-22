package com.yufeng.mapper;

import com.yufeng.model.vo.CompanyInfoVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 企业表 Mapper 接口
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Repository // 有了这个注解，mybatis才能帮你自动代理成一个Bean
public interface CompanyMapperCustom {


    /**
     * 根据查询条件查询公司信息列表
     *
     * @param paramMap 查询参数
     * @return 返回公司信息列表
     */
    public List<CompanyInfoVO> queryCompanyInfoList(@Param("paramMap") Map<String, Object> paramMap);



    /**
     * 根据公司ID查询公司信息
     *
     * @param companyId 公司ID
     * @return 返回公司信息
     */
    public CompanyInfoVO getCompanyInfo(@Param("companyId") String companyId);
}
