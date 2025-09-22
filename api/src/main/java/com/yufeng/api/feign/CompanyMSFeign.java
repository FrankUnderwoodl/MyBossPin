package com.yufeng.api.feign;

import com.yufeng.model.bo.SearchBO;
import com.yufeng.grace.result.GraceJSONResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Lzm
 * @CreateTime 2025年7月30日 02:39
 * @describe
 */
@FeignClient("company-service")  // 本接口为调用work-service的Feign客户端(声明式客户端远程调用)
public interface CompanyMSFeign {

    /**
     * 我给你一堆的companyId，你帮我把这些公司的HR列表都查询出来
     * @param searchBO 查询条件
     * @return 返回公司列表
     */
    @PostMapping("/company/list/get")
    GraceJSONResult getCompanyList(@RequestBody SearchBO searchBO);

}
