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
import java.util.UUID;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 * 这是SaaS平台的登录授权中心控制器，主要用于生成二维码登录的Token。
 * 这里记得让gateway放行，因为未登录，哪来的权限验证？
 */
@RestController
@RequestMapping("/saas")
@Slf4j
public class SaasPassportController extends BaseInfoProperties {

    @PostMapping("/getQRToken")
    public GraceJSONResult getQRToken() {

        // 1.生成一个随机的UUID作为QR Token
        String qrToken = UUID.randomUUID().toString(); // 通过UUID生成几乎不可能重复的标识符
        log.info("Generated QR Token: {}", qrToken);

        // 2.把生成的QR Token存储到Redis中(为了安全，设定一个过期时间)
        redis.set(SAAS_PLATFORM_LOGIN_TOKEN + ":" + qrToken, qrToken, 60 * 30); // 设置过期时间为30分钟

        // 3.将刚生成的token在Redis中标记为未使用状态，因为网页端会每隔几秒钟轮询一次。直到手机扫码后，将Redis设置为已使用状态，这时前端界面会轮询查询后端该redis的值是否设置了1(已使用状态)，如果是1，则前端界面会跳转到登录成功的页面。
        redis.set(SAAS_PLATFORM_LOGIN_TOKEN_READ + ":" + qrToken, "0", 60 * 30);

        // 返回生成的QR Token给前端，让下一次前端轮询时可以带上这个qrToken
        return GraceJSONResult.ok(qrToken);
    }
}
