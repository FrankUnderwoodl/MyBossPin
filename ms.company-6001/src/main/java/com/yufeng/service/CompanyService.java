package com.yufeng.service;

import com.yufeng.model.bo.CreateCompanyBO;
import com.yufeng.model.bo.ModifyCompanyInfoBO;
import com.yufeng.model.bo.QueryCompanyBO;
import com.yufeng.model.bo.ReviewCompanyBO;
import com.yufeng.model.pojo.Company;
import com.yufeng.model.pojo.CompanyPhoto;
import com.yufeng.utils.PagedGridResult;
import com.yufeng.model.vo.CompanyInfoVO;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 企业表 服务类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
public interface CompanyService {


    /**
     * 根据公司全称查询公司信息
     *
     * @param fullName 公司全称
     * @return 返回查询结果
     */
    public Company getByFullName(String fullName);




    /**
     * 创建新的公司(状态：未发起审核认证)
     *
     * @param createCompanyBO 创建公司业务对象
     * @return                返回新创建的公司的ID
     */
    public String createNewCompany(CreateCompanyBO createCompanyBO);


    /**
     * 重新发起审核，修改企业
     * @param createCompanyBO 创建公司业务对象
     */
    public String resetNewCompany(CreateCompanyBO createCompanyBO);


    /**
     * 根据公司ID查询公司详情
     *
     * @param companyId 公司ID
     * @return 返回查询结果
     */
    public Company getCompanyById(String companyId);




    /**
     * 提交审核的企业信息
     *
     * @param reviewCompanyBO 审核公司业务对象
     */
    public void commitReviewCompanyInfo(ReviewCompanyBO reviewCompanyBO);




    /**
     * 查询公司列表
     *
     * @param companyBO 查询公司业务对象
     * @param page           当前页码
     * @param limit          每页记录数
     * @return 返回分页结果
     */
    public PagedGridResult queryCompanyList(QueryCompanyBO companyBO, Integer page, Integer limit);



    /**
     * 根据企业ID查询数据库获得最新企业信息
     *
     * @param companyId 企业ID
     * @return 返回查询结果
     */
    public CompanyInfoVO getCompanyInfo(String companyId);



    /**
     * 更新审核后的信息
     *
     * @param reviewCompanyBO 审核公司业务对象
     */
    public void updateReviewInfo(ReviewCompanyBO reviewCompanyBO);


    /**
     * 更新企业信息
     *
     * @param modifyCompanyInfoBO 企业对象
     */
    public void updateCompanyInfo(ModifyCompanyInfoBO modifyCompanyInfoBO);



    /**
     * 保存企业照片
     *
     * @param modifyCompanyInfoBO 企业信息业务对象
     */
    public void savePhotos(ModifyCompanyInfoBO modifyCompanyInfoBO);




    /**
     * 根据公司ID查询企业照片
     *
     * @param companyId 公司ID
     * @return 返回企业照片对象
     */
    public CompanyPhoto getPhotos(String companyId);



    /**
     * 我给你一堆的companyId，你帮我把这些公司都变更为Company对象
     * 供给work服务调用
     *
     * @param companyIds 公司ID列表
     * @return 返回公司列表
     */
    List<Company> getCompanyList(List<String> companyIds);



    /**
     * 根据companyId查询该企业是否是VIP
     * @param companyId 企业ID
     * @return true/false
     */
    Boolean getIsVip(String companyId);



    /**
     * 设置企业为VIP
     * @param companyId 企业ID
     * @param date  过期时间
     */
    public void setCompanyVip(String companyId, LocalDate date);
}
