package com.yufeng.api.feign.fallback;

import com.yufeng.api.feign.UserInfoMSFeign;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.model.bo.SearchBO;
import com.yufeng.model.pojo.Users;
import org.springframework.stereotype.Component;

/**
 * @author Lzm
 * @CreateTime 2025年9月22日 09:36
 */
@Component
public class UserInfoMSFeignFallback implements UserInfoMSFeign {

    @Override
    public GraceJSONResult getHRCountsByCompanyId(String companyId) {
        return null;
    }

    @Override
    public GraceJSONResult bindHRToCompany(String hrUserId, String realname, String companyId) {
        return null;
    }

    // 如果user微服务宕机了，这个方法会执行(兜底、降级处理)
    @Override
    public GraceJSONResult get(String userId) {

        // 返回一个空对象，或者返回一个error信息呗
        // 这里返回一个空对象
        Users users = new Users();
        users.setId(userId);
        return GraceJSONResult.ok(users);
    }

    @Override
    public GraceJSONResult changeUserToHR(String hrUserId) {
        return null;
    }

    @Override
    public GraceJSONResult getHRList(SearchBO searchBO) {
        return null;
    }
}
