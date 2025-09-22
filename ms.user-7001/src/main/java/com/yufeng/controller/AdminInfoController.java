package com.yufeng.controller;

import com.yufeng.api.interceptor.JWTUserInterceptor;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.model.bo.CreateAdminBo;
import com.yufeng.model.bo.ResetPwdBo;
import com.yufeng.model.bo.UpdateAdminBO;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.model.pojo.Admin;
import com.yufeng.service.AdminService;
import com.yufeng.utils.PagedGridResult;
import com.yufeng.model.vo.AdminInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 */
@RestController
@RequestMapping("/admininfo")
@Slf4j
public class AdminInfoController extends BaseInfoProperties {

    // 注入service
    @Autowired
    private AdminService adminService;

    /**
     * 管理员登录接口
     *
     * @return 返回登录结果
     * 是的，@RequestBody 注解可以将前端传来的 JSON 字符串自动转换成 AdminBo 对象，前提是 AdminBo 类中有对应的 username 和 password 属性。
     */
    @PostMapping("/create")
    public GraceJSONResult create(@Valid @RequestBody CreateAdminBo createAdminBo) {

        // 调用service层的创建管理员方法
        adminService.createAdmin(createAdminBo);

        return GraceJSONResult.ok();
    }



    /**
     * 管理员登录接口
     *
     * @param accountName 要查询的管理员账号名
     * @param page        当前页数
     * @param limit       每页限制的记录数
     *                    defaultValue: 当请求中传递了 page 参数但值为空字符串时，就会使用默认值 1
     * @return 返回登录结果
     */
    @PostMapping("/list")
    public GraceJSONResult list(@RequestParam String accountName,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer limit) {

        // 调用service层的查询管理员列表方法
        PagedGridResult pagedGridResult = adminService.getAdminList(accountName, page, limit);

        return GraceJSONResult.ok(pagedGridResult);
    }




    /**
     * 删除某一个管理员的接口
     */
    @PostMapping("/delete")
    public GraceJSONResult delete(@RequestParam String username) {

        adminService.deleteAdmin(username);
        return GraceJSONResult.ok();
    }



    /**
     * 重置管理员密码的接口
     * 这里的重置密码是指删除该管理员账号，重新创建一个新的账号？
     *
     * @return 返回操作结果
     */
    @PostMapping("/resetPwd")
    public GraceJSONResult resetPwd(@Valid @RequestBody ResetPwdBo resetPwdBo) {

        // 常规方式: ①校验BO、②调用Service层的重置密码方法
        // AR模式，直接就在Controller中处理了，也就是在BO里面直接操作，但是底层需要借助Mapper。
        resetPwdBo.modifyPwd();

        return GraceJSONResult.ok();
    }

    /**
     * 获取当前登录管理员的信息
     *
     * @return 返回管理员信息
     */
    @PostMapping("/myInfo")
    public GraceJSONResult myInfo() {

        // 通过ThreadLocal获取当前登录的管理员信息
        Admin admin = JWTUserInterceptor.adminUser.get();

        // 问：为什么不直接返回ThreadLocal中的admin对象，而是通过service层的getById方法获取？
        // 答：因为ThreadLocal里面存的是cookie信息，有可能是过期的或者不完整的，所以需要通过service层的getById方法重新查询数据库获取最新的管理员信息。
        Admin adminInfo = adminService.getById(admin.getId());

        AdminInfoVO adminInfoVO = new AdminInfoVO();
        BeanUtils.copyProperties(adminInfo, adminInfoVO);

        return  GraceJSONResult.ok(adminInfoVO);
    }




    @PostMapping("updateMyInfo")
    public GraceJSONResult updateMyInfo(@RequestBody @Valid UpdateAdminBO adminBO) {

        // 获取当前登录的管理员信息
        Admin admin = JWTUserInterceptor.adminUser.get();

        // 将当前登录的管理员ID设置到adminBO中
        adminBO.setId(admin.getId());
        adminService.updateAdmin(adminBO);

        return GraceJSONResult.ok();
    }

}
