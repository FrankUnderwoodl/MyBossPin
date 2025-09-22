package com.yufeng.controller;

import com.google.gson.Gson;
import com.yufeng.api.feign.UserInfoMSFeign;
import com.yufeng.api.interceptor.JWTUserInterceptor;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.enums.CompanyReviewStatus;
import com.yufeng.exception.GraceException;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.model.bo.*;
import com.yufeng.model.pojo.Company;
import com.yufeng.model.pojo.Users;
import com.yufeng.service.CompanyService;
import com.yufeng.utils.GsonUtils;
import com.yufeng.utils.JsonUtils;
import com.yufeng.utils.PagedGridResult;
import com.yufeng.model.vo.CompanyInfoVO;
import com.yufeng.model.vo.CompanySimpleVO;
import com.yufeng.model.vo.UsersVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 */
@Slf4j
@RestController
@RequestMapping("/company")
public class CompanyController extends BaseInfoProperties {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserInfoMSFeign userInfoMSFeign;


// =================================以下是提供给App端的接口(主要用来查看企业)========================================

    /**
     * 根据公司全称查询公司信息(当公司注册时，用户需要输入的公司全称)
     *
     * @param fullName 公司全称
     * @return 返回查询结果
     */
    @PostMapping("/getByFullName")
    public GraceJSONResult getByFullName(@RequestParam String fullName) {

        // log.info("根据公司全称查询公司信息，fullName: {}", fullName);

        // 检查 fullName 是否为空
        if (StringUtils.isBlank(fullName)) {
            return GraceJSONResult.error();
        }

        // 向数据库查询是否存在该公司所对应的记录(这里通过公司名来查询MySQL，已经设置了索引，很快的)
        Company company = companyService.getByFullName(fullName);

        // 如果查询结果为空，直接返回空结果
        if (company == null) {
            return GraceJSONResult.ok(null);
        }

        // new一个VO，给前端进行展示
        CompanySimpleVO companySimpleVO = new CompanySimpleVO();
        BeanUtils.copyProperties(company, companySimpleVO);

        return GraceJSONResult.ok(companySimpleVO);
    }


    /**
     * 供给App端用户创建新公司，或者被admin端打回之后，用户再次进行新的公司信息创建
     * <p>
     * {
     * "companyId": "COMP001",
     * "companyName": "北京科技有限公司",
     * "shortName": "北京科技",
     * "logo": "https://example.com/logo.png",
     * "bizLicense": "有限责任公司",
     * "peopleSize": "100-500人",
     * "industry": "信息技术",
     * "reviewStatus": 3
     * }
     *
     * @param createCompanyBO 创建公司业务对象
     * @return 返回创建、更新之后的公司ID
     */
    @PostMapping("/createNewCompany")
    public GraceJSONResult createNewCompany(@RequestBody CreateCompanyBO createCompanyBO) {

        String doCompanyId;

        // 判断两种情况：第一次创建公司(id：空)     公司审核不过关(id：不为空)
        String companyId = createCompanyBO.getCompanyId();
        if (companyId == null) {
            // 第一次创建公司
            doCompanyId = companyService.createNewCompany(createCompanyBO);
        } else {
            // 重置公司信息(被运营人员审核不通过的)
            doCompanyId = companyService.resetNewCompany(createCompanyBO);
        }

        return GraceJSONResult.ok(doCompanyId);
    }


    /**
     * 根据公司ID查询某公司详情
     * 用于App端展示当前公司的logo、入驻的HR等信息(在App端中修改为HR时需要查询该公司的信息)
     *
     * @param companyId    公司ID
     * @param withHRCounts 是否需要查询该公司的HR数量
     * @return 目标是返回一个CompanySimpleVO对象(就多了HR Counts这个字段)，用于给App端进行展示
     */
    @PostMapping("/getInfo")
    @GlobalTransactional
    public GraceJSONResult getInfo(String companyId, Boolean withHRCounts) {

        // 从Redis/MySQL中获取公司简单信息
        CompanySimpleVO companySimpleVO = this.getCompanySimpleVO(companyId);

        // 再根据companyId去调用用户微服务，查到有多少个用户挂在这个company上(前提是该companyId有对应的记录并且withHRCounts=true)
        if (withHRCounts == true && companySimpleVO != null) {

            // 正式调用HR微服务查询该公司下的HR数量
            GraceJSONResult result = userInfoMSFeign.getHRCountsByCompanyId(companyId);
            if (result.getStatus() == 200) {
                // 如果查询成功，将HR数量设置到companySimpleVO中
                Long hrCounts = Long.valueOf(result.getData().toString());
                companySimpleVO.setHrCounts(hrCounts);
            } else {
                // 如果查询失败，设置HR数量为0
                companySimpleVO.setHrCounts(0L);
            }

        }

        return GraceJSONResult.ok(companySimpleVO);
    }


    /**
     * 通过公司ID，从redis/MySQL中获取公司简单的信息
     * {
     * "id": "COMP001",
     * "companyName": "北京科技有限公司",
     * "shortName": "北京科技",
     * "logo": "https://example.com/logo/company-logo.png",
     * "peopleSize": "100-500人",
     * "industry": "互联网/软件开发",
     * "nature": "民营企业",
     * "address": "北京市朝阳区中关村科技园",
     * "reviewStatus": 1,
     * "reviewReplay": "审核通过，企业信息完整",
     * "hrCounts": 3
     * }
     * 如果没有，则从数据库中查询并放入redis中
     *
     * @param companyId 公司ID
     * @return 返回查询结果
     */
    private CompanySimpleVO getCompanySimpleVO(String companyId) {

        // 检查companyId是否为空
        if (StringUtils.isBlank(companyId)) {
            return null; // 如果公司ID为空，直接返回null
        }

        String companyJson = redis.get(REDIS_COMPANY_BASE_INFO + ":" + companyId);
        if (StringUtils.isBlank(companyJson)) { // 如果redis中没有公司信息
            // 从数据库中查询公司信息
            Company company = companyService.getCompanyById(companyId);
            if (company == null) {
                return null; // 如果数据库中也没有公司信息，返回null
            }
            // 将Company对象转换为CompanySimpleVO对象
            CompanySimpleVO companySimpleVO = new CompanySimpleVO();
            BeanUtils.copyProperties(company, companySimpleVO);

            // 将CompanySimpleVO对象转换为JSON字符串并存入redis
            redis.set(REDIS_COMPANY_BASE_INFO + ":" + companyId, GsonUtils.object2String(companySimpleVO), 60 * 60 * 24); // 设置缓存时间为一天

            return companySimpleVO;
        } else {
            // 将JSON字符串转换为CompanySimpleVO对象
            return GsonUtils.stringToBean(companyJson, CompanySimpleVO.class);
        }
    }


    /**
     * 将公司置为审核中状态，并绑定当前用户HR到公司(其实就是设置了commit_user_id、commit_user_mobile字段到记录中)
     * var bo = {
     * hrUserId: this.companyInfo.commitUserId,    // 申请人用户ID
     * companyId: this.companyInfo.companyId,      // 企业ID
     * reviewReplay: this.reviewReplay,            // 审核意见
     * reviewStatus: status,                       // 审核结果状态
     * }
     *
     * @param reviewCompanyBO 审核公司业务对象
     * @return 返回审核结果
     */
    @PostMapping("/goReviewCompany")
    @GlobalTransactional
    public GraceJSONResult goReviewCompany(@RequestBody ReviewCompanyBO reviewCompanyBO) {

        // 0. 检查参数是否完整


        // 1. 调用用户微服务，将当前HR绑定到对应的公司(其实就是设置users表中的hr_in_which_company_id、real_name字段)
        GraceJSONResult result = userInfoMSFeign.bindHRToCompany(reviewCompanyBO.getHrUserId(),
                reviewCompanyBO.getRealname(),
                reviewCompanyBO.getCompanyId());

        // 设置HR的手机号进到BO中，进行保存
        String hrMobile = result.getData().toString();
        reviewCompanyBO.setHrMobile(hrMobile);

        // 2.将公司审核信息保存进company表中，修改状态为[3：审核中（等待审核）] 「针对company表」
        companyService.commitReviewCompanyInfo(reviewCompanyBO);

        // 因为是分布式事务，所以需要考虑事务的回滚
        // 模仿一个异常，来测试分布式事务
        // int a = 1 / 0; // 模拟异常，测试分布式事务

        return GraceJSONResult.ok();
    }


    /**
     * 根据hr的用户id查询最新的企业信息(用于App端的‘我要找人’功能)
     *
     * @param hrUserId HR用户ID
     * @return 返回查询结果
     */
    @PostMapping("information")
    public GraceJSONResult information(String hrUserId) {

        // 调用用户微服务获取HR用户最新的信息，因为需要拿到hr_in_which_company_id
        UsersVO hrUser = this.getHRInfoVO(hrUserId);

        // 企业的信息
        CompanySimpleVO company = getCompanySimpleVO(hrUser.getHrInWhichCompanyId());

        return GraceJSONResult.ok(company);
    }

    /**
     * 获取HR的用户信息
     *
     * @param hrUserId HR用户ID
     * @return 返回HR用户信息
     */
    private UsersVO getHRInfoVO(String hrUserId) {

        // 调用用户微服务获取HR用户信息
        GraceJSONResult jsonResult = userInfoMSFeign.get(hrUserId);
        Object data = jsonResult.getData();

        // 将获取到的用户信息转换为UsersVO对象
        String json = JsonUtils.objectToJson(data);
        // 使用JsonUtils将JSON字符串转换为UsersVO对象
        UsersVO hrUser = JsonUtils.jsonToPojo(json, UsersVO.class);
        return hrUser;
    }


// =============================以下是提供给admin端的接口(主要是用来审核企业的)============================================


    /**
     * 用于admin端查询公司列表
     *
     * @param queryCompanyBO 查询公司业务对象
     * @param page           页码
     * @param limit          每页条数
     * @return 返回admin端输入companyName、commitUser、reviewStatus、commitDateStart、commitDateEnd等条件查询的结果
     * QueryCompanyBO举例：
     * {
     * "companyName": "阿里巴巴集团",
     * "commitUser": "张三",
     * "reviewStatus": 1,
     * "commitDateStart": "2024-01-01",
     * "commitDateEnd": "2024-01-31"
     * }
     */
    @PostMapping("admin/getCompanyList")
    public GraceJSONResult adminGetCompanyList(@RequestBody QueryCompanyBO queryCompanyBO,
                                               @DefaultValue("1") Integer page,
                                               @DefaultValue("10") Integer limit) {

        // 调用service层查询公司列表
        PagedGridResult result = companyService.queryCompanyList(queryCompanyBO, page, limit);
        return GraceJSONResult.ok(result);
    }


    /**
     * admin端审核公司信息
     *
     * @param companyId 公司ID
     * @return 返回审核结果
     */
    @PostMapping("admin/getCompanyInfo")
    public GraceJSONResult getCompanyInfo(String companyId) {

        CompanyInfoVO companyInfoVO = companyService.getCompanyInfo(companyId);

        return GraceJSONResult.ok(companyInfoVO);
    }


    /**
     * 供admin端审核公司信息
     *
     * @param reviewCompanyBO 审核公司业务对象
     * @return 返回审核结果
     * var bo = {
     * hrUserId: this.companyInfo.commitUserId,    // 申请人用户ID
     * companyId: this.companyInfo.companyId,      // 企业ID
     * reviewReplay: this.reviewReplay,            // 审核意见
     * reviewStatus: status                        // 审核结果状态
     * }
     */
    @PostMapping("admin/doReview")
    public GraceJSONResult doReview(@Valid @RequestBody ReviewCompanyBO reviewCompanyBO) {

        // 1.将审核的结果更新到company表中
        companyService.updateReviewInfo(reviewCompanyBO);

        // 2.如果审核通过(也就是admin端将status设为了2)，则更新用户角色成为HR(又得调用用户微服务)
        if (reviewCompanyBO.getReviewStatus() == CompanyReviewStatus.SUCCESSFUL.type) {

            // 调用用户微服务，将当前HR绑定到对应的公司(其实就是设置users表中的hr_in_which_company_id、real_name字段)
            GraceJSONResult result = userInfoMSFeign.changeUserToHR(reviewCompanyBO.getHrUserId());
            // 检查是否绑定成功
            if (result.getStatus() != 200) {
                return GraceJSONResult.errorMsg("绑定HR到公司失败，请稍后重试");
            }
        }

        // 3.清除redis中该公司的简要缓存信息，因为审核后可能会有变更(reviewStatus、reviewReplay、hrCounts)
        redis.del(REDIS_COMPANY_BASE_INFO + ":" + reviewCompanyBO.getHrUserId());

        return GraceJSONResult.ok();
    }


    // =============================以下是提供给sass端、App端(用户端)的接口===============================================


    /**
     * sass端查询公司信息
     * 供给SaaS平台查询公司信息
     *
     * @return 返回查询结果
     */
    @PostMapping("info")
    public GraceJSONResult info() {
        // 从ThreadLocal中获取当前用户的信息，拿到里面的hr_in_which_company_id
        Users users = JWTUserInterceptor.currentUser.get();

        // 通过hr_in_which_company_id来查询公司信息
        CompanySimpleVO companySimpleVO = this.getCompanySimpleVO(users.getHrInWhichCompanyId());

        return GraceJSONResult.ok(companySimpleVO);
    }


    /**
     * sass端查询更多公司信息
     * 供给SaaS平台查询公司更多信息
     *
     * @return 返回查询结果
     */
    @PostMapping("saas/moreInfo")
    public GraceJSONResult saasMoreInfo() {

        // 从ThreadLocal中获取当前用户的信息，拿到里面的hr_in_which_company_id
        Users currentUser = JWTUserInterceptor.currentUser.get();
        CompanyInfoVO companyInfo = getCompanyMoreInfo(currentUser.getHrInWhichCompanyId());
        return GraceJSONResult.ok(companyInfo);
    }

    /**
     * App用户端获得查询企业详情(先从redis中获取，如果没有则从数据库中获取)
     *
     * @return 返回查询结果
     */
    @PostMapping("moreInfo")
    public GraceJSONResult moreInfo(String companyId) {
        CompanyInfoVO companyInfo = getCompanyMoreInfo(companyId);
        return GraceJSONResult.ok(companyInfo);
    }

    // 在redis或者数据库中获取公司更多信息
    private CompanyInfoVO getCompanyMoreInfo(String companyId) {
        if (StringUtils.isBlank(companyId)) return null;

        String companyJson = redis.get(REDIS_COMPANY_MORE_INFO + ":" + companyId);
        if (StringUtils.isBlank(companyJson)) {
            // 查询数据库
            Company company = companyService.getCompanyById(companyId);
            if (company == null) {
                return null;
            }

            CompanyInfoVO infoVO = new CompanyInfoVO();
            BeanUtils.copyProperties(company, infoVO);

            redis.set(REDIS_COMPANY_MORE_INFO + ":" + companyId, new Gson().toJson(infoVO), 60 * 60 * 24); // 设置缓存时间为一天
            return infoVO;
        } else {
            // 不为空，直接转换对象
            return new Gson().fromJson(companyJson, CompanyInfoVO.class);
        }
    }


    /**
     * 修改公司信息
     * 供给App端的HR用户去修改公司信息
     * 举个例子，修改企业的短名：
     * var pendingInfo = {
     * "currentUserId": userId,
     * "companyId": companyId,
     * "shortName": companyShortName
     * };
     *
     * @param modifyCompanyInfoBO 修改公司信息业务对象
     * @return 返回修改结果
     */
    @PostMapping("modify")
    public GraceJSONResult modify(@RequestBody @Valid ModifyCompanyInfoBO modifyCompanyInfoBO) {

        // 1.需要判断传进来的companyId是否和当前用户的hr_in_which_company_id一致，防止有人刷接口去篡改企业信息
        this.checkUser(modifyCompanyInfoBO.getCurrentUserId(), modifyCompanyInfoBO.getCompanyId());

        // 2.正式调用service层修改公司信息
        companyService.updateCompanyInfo(modifyCompanyInfoBO);

        // 判断是不是需要上传公司相册
        if (StringUtils.isNotBlank(modifyCompanyInfoBO.getPhotos())) {
            companyService.savePhotos(modifyCompanyInfoBO);
        }

        return GraceJSONResult.ok();
    }

    private void checkUser(String currentUserId, String companyId) {

        // 检查当前用户ID和公司ID是否为空
        if (StringUtils.isBlank(currentUserId) || StringUtils.isBlank(companyId)) {
            GraceException.doException(ResponseStatusEnum.COMPANY_INFO_UPDATED_ERROR);
        }
        // 从用户微服务中获取当前用户信息
        UsersVO hrInfoVO = getHRInfoVO(currentUserId);
        // 检查当前用户的hr_in_which_company_id是否和传入的companyId一致
        if (hrInfoVO == null || !hrInfoVO.getHrInWhichCompanyId().equals(companyId)) {
            // 如果不一致，抛出异常
            GraceException.doException(ResponseStatusEnum.COMPANY_INFO_UPDATED_ERROR);
        }
    }


    /**
     * 获得企业相册内容，针对App端
     */
    @PostMapping("getPhotos")
    public GraceJSONResult getPhotos(String companyId) {
        return GraceJSONResult.ok(companyService.getPhotos(companyId));
    }


    /**
     * 获得企业相册内容，针对SaaS平台
     */
    @PostMapping("saas/getPhotos")
    public GraceJSONResult getPhotosSaas() {
        String companyId = JWTUserInterceptor.currentUser.get()
                .getHrInWhichCompanyId();
        return GraceJSONResult.ok(companyService.getPhotos(companyId));
    }


    /**
     * 我给你一堆的companyId，你帮我把这些用户都变更为CompanyInfoVO
     * 供给work服务调用
     */
    @PostMapping("list/get")
    public GraceJSONResult getCompanyList(@RequestBody SearchBO searchBO) {

        // 获取所有的用户pojo
        List<Company> companyList = companyService.getCompanyList(searchBO.getCompanyIds());
        // 将所有的pojo转成vo
        List<CompanyInfoVO> companyInfoVOList =
                companyList.stream().map(company -> {
                    CompanyInfoVO companyInfoVO = new CompanyInfoVO();
                    BeanUtils.copyProperties(company, companyInfoVO);
                    // 因为companyInfoVO里面的companyId，而pojo是id，所以要单独设置一下
                    companyInfoVO.setCompanyId(company.getId());
                    return companyInfoVO;
                    // collect 是一个终结操作，用于将 Stream 中的所有元素收集到一个容器中，Collectors.toList() 是一个收集器，它告诉 collect 方法要将 Stream 中的元素收集到一个 List 中。
                }).collect(Collectors.toList());

        // 将List转成一个String
        String companyListStr = GsonUtils.object2String(companyInfoVOList);

        return GraceJSONResult.ok(companyListStr);
    }


    /**
     * 判断某个公司是否是VIP，提供给App端使用，开了会员之后可以让HR可以有更多的查询范围～
     * @param companyId 公司ID
     * @return 返回查询结果
     */
    @PostMapping("isVip")
    public GraceJSONResult isVip(String companyId) {

        if (StringUtils.isBlank(companyId)) {
            return GraceJSONResult.ok(false);
        }

        return GraceJSONResult.ok(companyService.getIsVip(companyId));
    }


    /**
     * 判断当前登录的公司是否是VIP，提供给SaaS端使用
     * @return 返回查询结果
     */
    @PostMapping("saas/isVip")
    public GraceJSONResult isVipSaas() {

        Users user = JWTUserInterceptor.currentUser.get();
        String companyId = user.getHrInWhichCompanyId();

        if (StringUtils.isBlank(companyId)) {
            return GraceJSONResult.ok(false);
        }

        return GraceJSONResult.ok(companyService.getIsVip(companyId));
    }

}
