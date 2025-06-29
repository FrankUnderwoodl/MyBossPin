package com.yufeng.controller;

import com.google.gson.Gson;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.bo.RegisterLoginBo;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.pojo.Users;
import com.yufeng.service.UsersService;
import com.yufeng.utils.IPUtil;
import com.yufeng.utils.JWTUtil;
import com.yufeng.utils.SMSUtils;
import com.yufeng.vo.UsersVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 *
 */
@RestController
@RequestMapping("/passport")
@Slf4j
@Api(tags = "PassportController 授权中心接口模块") // 这个注解可以用来定义接口类的名字
public class AppPassportController extends BaseInfoProperties {

    // 注入短信工具类
    @Autowired
    private SMSUtils smsUtils;

    // 注入service层
    @Autowired
    UsersService usersService;

    // 注入jwt工具类
    @Autowired
    JWTUtil jwtUtil;

    /**
     * 加@RequestParam的功能是什么呢？
     * 加上 @RequestParam 的情况：
     * · 可以明确指定参数来源于请求参数
     * · 默认情况下参数是必需的，如果请求中没有该参数会返回 400 错误
     * · 可以通过 required 属性设置参数是否必需：@RequestParam(required = false)
     * · 可以通过 defaultValue 属性设置默认值
     * · 可以通过 value 或 name 属性指定参数名称，使之与方法参数名称不同
     */
    @PostMapping("/getSMSCode")
    public GraceJSONResult getSMSCode(String mobile, HttpServletRequest request) {
        // 校验手机号是否为空
        if (mobile == null || mobile.isEmpty()) {
            return GraceJSONResult.error();
        }

        // 生成验证码
        String code = (int)((Math.random() * 9 + 1) * 100000) + "";
        log.info("手机号：{}，验证码：{}", mobile, code);

        // 将验证码存入Redis，设置过期时间为30分钟
        redis.set(MOBILE_SMSCODE + ":" + mobile, code, 30 * 60);

        // 限制用户在60秒内只能获取一次验证码(通过限制IP的方式)
        String userIp = IPUtil.getRequestIp(request).replace(":", ".");
        // 这里类似使用了锁的机制，也就是把这个key当成锁来使用(锁住了60秒)
        redis.setnx60s(MOBILE_SMSCODE + ":" + userIp, mobile);

        // 这里可以调用短信发送方法，可惜我没有
        // smsUtils.sendSMS(mobile, code);

        return GraceJSONResult.ok();
    }

    /**
     * 用户登录注册接口
     */
    @PostMapping("/login")
    public GraceJSONResult login(@Valid @RequestBody RegisterLoginBo registerLoginBo,
                                                     HttpServletRequest request) {

        // 校验手机号和验证码是否为空已经通过@Valid注解完成了

        // 首先获取手机号和验证码
        String smsCode = registerLoginBo.getSmsCode();
        String mobile = registerLoginBo.getMobile();

        // 1. 从Redis中获取验证码，并对前端传来的验证码进行校验
        String redisCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(smsCode)) { // 将来验证码可能会区分大小写，所以使用equalsIgnoreCase
            // 验证码不匹配，返回错误信息
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        // 2. 根据mobile这个字段查询用户是否存在，如果不存在则注册新用户(调用service层方法，往MySQL中插数据)
        Users user = usersService.queryUserIsExist(mobile);
        if (user == null) {
            // 如果用户不存在，那就创建新用户咯(再次调用service层方法)
            user = usersService.createUser(mobile);
        }

        // 3. 用户登录注册后，马上删除Redis中的验证码(防止其他人二次使用)
        redis.del(MOBILE_SMSCODE + ":" + mobile); // 删除验证码

        // 4. 设置用户的token，存入Redis中(让每一个微服务都能获取到用户信息)、或者设置jwt
        // String userToken = TOKEN_USER_PREFIX + SYMBOL_DOT +  UUID.randomUUID().toString(); // 为app端而生
        // redis.set(REDIS_USER_TOKEN + ":" + user.getId(), userToken, 60 * 60 * 24 * 7); // 设置token的过期时间为7天

        // 这里使用JWT生成token，createJWTWithPrefix参数分别是：1.传输的json、2.前端的前缀、3.过期时间(单位为毫秒)
        String jwt = jwtUtil.createJWTWithPrefix(new Gson().toJson(user), TOKEN_APP_PREFIX,
                (long) (60 * 60 * 24 * 60 * 1000)); // 设置过期时间为60天

        // 5. 将token存入VO中，返回给前端保存使用
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO); // 将user对象的属性复制到usersVO中
        usersVO.setUserToken(jwt); // 设置用户的token、JWTToken

        return GraceJSONResult.ok(usersVO);
    }



    /**
     * 用户登出接口
     */
    @PostMapping("/logout")
    public GraceJSONResult logout(@RequestParam String userId, HttpServletRequest request) {

        // 清除Redis中的用户token(现在是通过JWT来管理用户登录状态的，所以这里不需要清除token)
        // String userToken = REDIS_USER_TOKEN + ":" + userId;
        // redis.del(userToken); // 删除用户的token

        return  GraceJSONResult.ok();
    }
}
