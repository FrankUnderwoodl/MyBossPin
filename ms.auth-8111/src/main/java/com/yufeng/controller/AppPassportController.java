package com.yufeng.controller;

import com.google.gson.Gson;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.model.bo.RegisterLoginBo;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.api.mq.SMSConfig;
import com.yufeng.model.mq.SMSContentQO;
import com.yufeng.model.pojo.Users;
import com.yufeng.service.UsersService;
import com.yufeng.utils.GsonUtils;
import com.yufeng.utils.IPUtil;
import com.yufeng.utils.JWTUtil;
import com.yufeng.utils.SMSUtils;
import com.yufeng.model.vo.UsersVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.UUID;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 * @describe 授权中心接口模块，也就是给移动端提供的登录注册接口(给前端提供JWT)
 */
@RestController // 这个注解可厉害了，它会自动将返回值转换为JSON字符串，并且会自动添加@ResponseBody注解
@RequestMapping("/passport")
@Slf4j
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

    // 注入RabbitMQ的消息发送工具类，你可以跟RedisTemplate一样使用它来发送消息到RabbitMQ
    // Spring Boot 自动配置：当项目中引入了 spring-boot-starter-amqp 依赖时，Spring Boot 会自动配置 RabbitMQ 相关的 Bean，包括 RabbitTemplate
    @Autowired
    private RabbitTemplate rabbitTemplate;


    /**
     * 加@RequestParam的功能是什么呢？
     * 加上 @RequestParam 的情况：
     * • 可以明确指定参数来源于请求参数
     * • 默认情况下参数是必需的，如果请求中没有该参数会返回 400 错误
     * • 可以通过 required 属性设置参数是否必需：@RequestParam(required = false)
     * • 可以通过 defaultValue 属性设置默认值
     * • 可以通过 value 或 name 属性指定参数名称，使之与方法参数名称不同
     */
    @PostMapping("/getSMSCode")
    public GraceJSONResult getSMSCode(String mobile, HttpServletRequest request) throws Exception {

        // 校验手机号是否为空
        if (StringUtils.isBlank(mobile)) {
            return GraceJSONResult.error();
        }

        // 生成验证码
        String code = (int) ((Math.random() * 9 + 1) * 100000) + "";
        log.info("手机号为：{}，收到的验证码为：{}", mobile, code);

        // 这里使用RabbitMQ来发送短信验证码，也就是异步解耦，不用担心发送短信的时间过长影响用户体验(也就是让这个接口可以马上返回)
        SMSContentQO contentQO = new SMSContentQO();
        contentQO.setMobile(mobile);
        contentQO.setContent(code);


        // 当消息成功发送到RabbitMQ中间件时，会触发这个回调函数，一般在生产者端使用：confirmCallback
        // 回调函数的参数为：1.消息的相关信息(关联数据)、2.是否成功、3.失败原因(ack为true的话，cause就为null)
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("交换机成功接收到消息，消息ID: {}", correlationData.getId());
            } else {
                log.error("交换机接收消息失败，消息ID: {}, 原因: {}", correlationData.getId(), cause);
            }
        });

        // 当消息到达了exchange，但是到达不了queue时，会触发这个回调函数，一般在生产者端使用：returnCallback
        // 回调函数的参数为：1.消息内容、2.回复码、3.回复文本、4.交换机名称、5.路由键
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {

            log.error("消息发送失败，消息内容: {}, 回复码: {}, 回复文本: {}, 交换机: {}, 路由键: {}",
                    message, replyCode, replyText, exchange, routingKey);
        });

        /*
        参数解释：
             1.交换机的名称：SMS_EXCHANGE
             2.路由Key：yufeng.sms.send.login
             3.消息内容：contentQO对象转换为JSON字符串
             4.关联数据：CorrelationData对象(就是一个UUID，用来标记消息的唯一性)
         */
        rabbitTemplate.convertAndSend(SMSConfig.SMS_EXCHANGE,      // 交换机名称
                SMSConfig.ROUTING_KEY_SMS_SEND_LOGIN,              // 路由Key
                GsonUtils.object2String(contentQO),                  // 消息内容(这里将contentQO对象转换为JSON字符串)
                new CorrelationData(UUID.randomUUID().toString()));  // 关联数据(这里使用UUID来标记消息的唯一性)


        // 来一个消息处理器processor(类对象)，可以设置消息的TTL
       /*  MessagePostProcessor postProcessor = message -> {
            message.getMessageProperties().setExpiration("20000"); // 设置消息的过期时间为20秒(单位为毫秒)，也就是20秒后该消息会被RabbitMQ自动删除
            return message;
        }; */

        // 发送消息到RabbitMQ中间件(该消息带有TTL)
       /*  for (int i = 0; i < 10; i++) {
            rabbitTemplate.convertAndSend(MQSMSConfig.SMS_EXCHANGE,
                    MQSMSConfig.ROUTING_KEY_SMS_SEND_LOGIN,
                    GsonUtils.object2String(contentQO),
                    postProcessor,
                    new CorrelationData(UUID.randomUUID().toString()));
        } */


        // 将验证码存入Redis，设置过期时间为30分钟
        redis.set(MOBILE_SMSCODE + ":" + mobile, code, 30 * 60);

        // 限制用户在60秒内只能获取一次验证码(通过限制IP的方式)
        String userIp = IPUtil.getRequestIp(request).replace(":", ".");
        // 这里类似使用了锁的机制，也就是把这个key当成锁来使用(锁住了60秒)
        redis.setnx60s(MOBILE_SMSCODE + ":" + userIp, mobile);

        return GraceJSONResult.ok();
    }



    /**
     * 用户登录注册接口
     * 如果登录成功，则返回用户信息和token给前端
     */
    @PostMapping("/login")
    public GraceJSONResult login(@Valid @RequestBody RegisterLoginBo registerLoginBo,
                                 HttpServletRequest request) {

        log.info("用户登录注册接口被调用，参数：{}", registerLoginBo);
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
        String jwt = jwtUtil.createJWTWithPrefix(new Gson().toJson(user), TOKEN_APP_PREFIX); // 设置过期时间为无限(不传过期时间参数)

        // 5. 将token这个值存入VO中，返回给前端保存使用
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO); // 将user对象的属性复制到usersVO中
        usersVO.setUserToken(jwt); // 设置用户的token

        log.info("用户登录成功，用户信息：{}", usersVO);
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

        return GraceJSONResult.ok();
    }
}
