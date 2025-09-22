package com.yufeng.controller;

import com.yufeng.api.interceptor.JWTUserInterceptor;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.model.bo.ModifyUserBO;
import com.yufeng.model.bo.SearchBO;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.model.pojo.Users;
import com.yufeng.service.UserService;
import com.yufeng.utils.GsonUtils;
import com.yufeng.utils.JWTUtil;
import com.yufeng.utils.PagedGridResult;
import com.yufeng.model.vo.UsersVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 */
@Slf4j
@RestController
@RequestMapping("/userinfo")
public class UserInfoController extends BaseInfoProperties {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTUtil jwtUtil;


//=========================================下面是供给APP端调用的接口================================================

    /**
     * 修改用户信息接口
     * 供给APP端调用
     *
     * @param userBO 用户信息修改对象
     * @return 返回最新的用户信息
     */
    @PostMapping("/modify")
    public GraceJSONResult modify(@RequestBody ModifyUserBO userBO) {

        // 调用service层方法来修改用户信息
        userService.modifyUserInfo(userBO);

        // 获取最新的用户信息
        UsersVO usersVO = this.getUserInfo(userBO.getUserId());
        return GraceJSONResult.ok(usersVO);
    }

    // 根据用户ID获取最新的用户信息
    private UsersVO getUserInfo(String userId) {

        // 查询最新的用户信息
        Users latestUsers = userService.getById(userId);
        // 生成新的的jwt token
        String jwtToken = jwtUtil.createJWTWithPrefix(GsonUtils.object2String(latestUsers), TOKEN_APP_PREFIX);

        UsersVO usersVO = new UsersVO();
        // 将最新的用户信息复制到VO对象中
        BeanUtils.copyProperties(latestUsers, usersVO);
        // 将新的token设置到VO对象中
        usersVO.setUserToken(jwtToken);

        return usersVO;
    }


    /**
     * 根据企业Id,来查询绑定该企业的HR数量有多少
     *
     * @param companyId 企业Id
     * @return 返回查询结果
     */
    @PostMapping("/getHRCountsByCompanyId")
    public GraceJSONResult getHRCountsByCompanyId(@RequestParam String companyId) {

        // 先从redis中查询，如果没有，再从db中查询后并且放入到redis中
        String hrCountsStr = redis.get(REDIS_COMPANY_HR_COUNTS + ":" + companyId);
        Long hrCounts;
        if (StringUtils.isBlank(hrCountsStr)) { // 缓存没有

            hrCounts = userService.getHRCountsByCompanyId(companyId);
            redis.set(REDIS_COMPANY_HR_COUNTS + ":" + companyId, String.valueOf(hrCounts), 60 * 30); // 缓存30分钟
        } else {
            hrCounts = Long.valueOf(hrCountsStr); // 缓存有
        }

        return GraceJSONResult.ok(hrCounts);
    }


    /**
     * 绑定HR到公司，其实就是更新用户的realname、hr_in_which_company_id字段
     *
     * @param hrUserId  HR用户ID
     * @param companyId 公司ID
     * @return 返回操作结果
     */
    @PostMapping("/bindHRToCompany")
    public GraceJSONResult bindHRToCompany(@RequestParam String hrUserId,
                                           @RequestParam String realname,
                                           @RequestParam String companyId) {

        // 先对参数进行校验
        // if (StringUtils.isBlank(hrUserId) || StringUtils.isBlank(realname) || StringUtils.isBlank(companyId)) {
        //     return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        // }

        // 调用service层方法来绑定HR到公司
        userService.updateUserCompanyId(hrUserId, realname, companyId);

        // 获取绑定后的HR用户信息
        Users hrUsers = userService.getById(hrUserId);

        // 返回HR的手机号
        return GraceJSONResult.ok(hrUsers.getMobile());
    }


    /**
     * 刷新用户信息
     *
     * @param userId 用户ID
     * @return 返回最新的用户信息
     */
    @PostMapping("/freshUserInfo")
    public GraceJSONResult freshUserInfo(@RequestParam String userId) {

        // 对参数进行校验
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        return GraceJSONResult.ok(getUserInfo(userId));
    }


    /**
     * 获取用户信息
     * 供给APP端调用(在mine界面中使用)
     *
     * @param userId 用户ID
     * @return 返回用户信息
     */
    @PostMapping("get")
    public GraceJSONResult get(@RequestParam String userId) {

        // 对参数进行校验
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        return GraceJSONResult.ok(getUserInfo(userId));
    }


    /**
     * 将用户变更为候选人
     * 供给APP端调用(在用户注册成功后，默认是普通用户，然后调用该接口将用户变更为候选人)
     *
     * @param hrUserId HR用户ID
     * @return 返回操作结果
     */
    @PostMapping("changeUserToCand")
    public GraceJSONResult changeUserToCand(@RequestParam String hrUserId) {
        userService.changeUserToCand(hrUserId);
        return GraceJSONResult.ok();
    }

//============================================下面是提供给SAAS端调用的接口================================================

    /**
     * 查询绑定该企业的HR列表
     * 供给Saas端调用
     *
     * @param page  当前页
     * @param limit 每页显示的条数
     * @return 返回查询结果
     */
    @PostMapping("saas/hrList")
    public GraceJSONResult changeUserToHR(Integer page, Integer limit) {

        // 先从ThreadLocal中获取当前登录的用户信息，然后再获取该用户的hrInWhichCompanyId字段
        Users users = JWTUserInterceptor.currentUser.get();
        String companyId = users.getHrInWhichCompanyId();
        PagedGridResult hrList = userService.getHRList(companyId, page, limit);
        return GraceJSONResult.ok(hrList);
    }


//=============================================下面是供给运营平台调用的接口================================================

    /**
     * 将用户变更为HR
     * 供给运营平台调用(也就是每当admin端将企业设为认证通过状态时，则就执行用户微服务，将commit的用户变更为HR、将role字段设为2)
     *
     * @param hrUserId HR用户ID
     * @return 返回操作结果
     */
    @PostMapping("changeUserToHR")
    public GraceJSONResult changeUserToHR(@RequestParam String hrUserId) {

        userService.changeUserToHR(hrUserId);
        return GraceJSONResult.ok();
    }


//=============================================下面是供给work服务调用的接口================================================


    /**
     * 我给你一堆的hrUserId，你帮我把这些用户都变更为UsersVO
     * 供给work服务调用
     *
     * @param searchBO 查询条件
     * @return 返回查询结果
     */
    @PostMapping("list/get")
    public GraceJSONResult getCompanyList(@RequestBody SearchBO searchBO) {

        // 获取所有的用户pojo
        List<Users> usersList = userService.getByIdList(searchBO.getUserIds());
        // 将所有的pojo转成vo
        List<UsersVO> usersVOList =
                usersList.stream().map(user -> {
                    UsersVO usersVO = new UsersVO();
                    BeanUtils.copyProperties(user, usersVO);
                    return usersVO;
                    // collect 是一个终结操作，用于将 Stream 中的所有元素收集到一个容器中，Collectors.toList() 是一个收集器，它告诉 collect 方法要将 Stream 中的元素收集到一个 List 中。
                }).collect(Collectors.toList());

        // 将List转成一个String
        String usersListStr = GsonUtils.object2String(usersVOList);

        return GraceJSONResult.ok(usersListStr);
    }

}
