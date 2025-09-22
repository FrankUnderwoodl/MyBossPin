package com.yufeng.api.feign;

import com.yufeng.api.feign.fallback.UserInfoMSFeignFallback;
import com.yufeng.model.bo.SearchBO;
import com.yufeng.grace.result.GraceJSONResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Lzm
 * @CreateTime 2025年7月30日 02:39
 * @describe
 */
@FeignClient(value = "user-service", fallback = UserInfoMSFeignFallback.class)  // 本接口为调用work-service的Feign客户端(声明式客户端远程调用)
public interface UserInfoMSFeign {


    /**
     * 根据公司ID查询HR统计信息
     * @param companyId 公司ID
     * @return 返回公司HR统计信息
     */
    @PostMapping("/userinfo/getHRCountsByCompanyId")
    public GraceJSONResult getHRCountsByCompanyId(@RequestParam String companyId);


    /**
     * 绑定公司与HR的关系
     * @param hrUserId HR用户ID
     * @param realname HR真实姓名
     * @param companyId 公司ID
     * @return 返回操作结果
     */
    @PostMapping("/userinfo/bindHRToCompany")
    public GraceJSONResult bindHRToCompany(@RequestParam String hrUserId, @RequestParam String realname,
                                           @RequestParam String companyId);


    /**
     * 根据用户ID查询用户信息
     * @param userId 用户ID
     * @return 返回用户信息
     */
    @PostMapping("/userinfo/get")
    public GraceJSONResult get(@RequestParam String userId);


    /**
     * 刷新用户信息成为HR
     * @param hrUserId HR用户ID
     * @return 返回操作结果
     */
    @PostMapping("/userinfo/changeUserToHR")
    public GraceJSONResult changeUserToHR(@RequestParam String hrUserId);



    /**
     * 我给你一堆的hrUserId，你帮我把这些用户都变更为HR
     * @param searchBO 查询条件
     * @return 返回HR列表
     */
    @PostMapping("/userinfo/list/get")
    public GraceJSONResult getHRList(@RequestBody SearchBO searchBO);



}
