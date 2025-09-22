package com.yufeng.controller;

import com.google.gson.Gson;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.model.bo.AdminBo;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.api.interceptor.JWTUserInterceptor;
import com.yufeng.model.pojo.Admin;
import com.yufeng.service.AdminService;
import com.yufeng.utils.JWTUtil;
import com.yufeng.model.vo.AdminVO;
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
@RequestMapping("/admin")
@Slf4j
public class AdminController extends BaseInfoProperties {

    // 注入service
    @Autowired
    private AdminService adminService;

    // 注入JWT工具类
    @Autowired
    private JWTUtil jWTUtil;



    /**
     * 管理员登录接口
     * @return 返回登录成功的jwt token
     * 问：SpringMVC是不是可以将前端传来的 JSON 字符串自动转换成 AdminBo 对象呀？
     * 答：是的，@RequestBody 注解可以将前端传来的 JSON 字符串自动转换成 AdminBo 对象，前提是 AdminBo 类中要有对应的 username 和 password 属性。反之，在axios中接收后端返回的json字符串时，也可以自动将其转换为JavaScript对象。
     */
    @PostMapping("/login")
    public GraceJSONResult adminLogin(@Valid @RequestBody AdminBo adminBo) {

        log.info("管理员登录请求，用户名：{}", adminBo.getUsername());

        // 1️⃣调用service的登录方法，判断管理员是否存在
        boolean isExist = adminService.adminLogin(adminBo);

        // 2️⃣如果不存在这个管理员，返回登录失败
        if (!isExist) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_LOGIN_ERROR);
        }

        // 3️⃣登录成功之后，获得该管理员信息，因为需要利用这个管理员信息去生成jwt token
        Admin adminInfo = adminService.getAdminInfo(adminBo);

        // 4️⃣将这个用户信息转成JSON格式
        String adminJson = new Gson().toJson(adminInfo); // 此处已将密码和盐进行JsonIgnore处理，或者你也可以重新定义一个VO，因为不同的公司有不同的规范嘛

        // 5️⃣生成jwt token
        String jwtToken = jWTUtil.createJWTWithPrefix(adminJson, TOKEN_ADMIN_PREFIX);

        return GraceJSONResult.ok(jwtToken);
    }



    /**
     * 获取管理员信息接口
     * 因为不管是H5、移动端，每次请求都会携带jwt token，所以可以直接通过这个直接就获取到用户信息
     * @return 返回管理员信息
     */
    @GetMapping("/info")
    public GraceJSONResult info() {

        // 因为api层已经通过JWTUserInterceptor拦截器将管理员信息存入了ThreadLocal中，所以这里可以直接获取
        Admin admin = JWTUserInterceptor.adminUser.get();

        // 将Admin对象转换为AdminVO对象
        AdminVO adminVO = new AdminVO();
        BeanUtils.copyProperties(admin, adminVO);

        return  GraceJSONResult.ok(adminVO);
    }



    @PostMapping("/logout")
    public GraceJSONResult logout() {

        return GraceJSONResult.ok("管理员已登出");
    }




}
