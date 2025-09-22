package com.yufeng.controller;

import com.google.gson.Gson;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.model.pojo.Users;
import com.yufeng.service.UsersService;
import com.yufeng.utils.JWTUtil;
import com.yufeng.model.vo.SaasUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.UUID;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 * @describe 1️⃣这里是SaaS平台的登录授权中心控制器，主要用于生成二维码登录的Token。
 * 2️⃣这里记得让gateway中的放行(配置进excludeUrlPath)，因为未登录，哪来的权限验证token呢？
 * 3️⃣这里会在redis中set两个值，一个是二维码登录的Token(用于App端扫码之后，进行判断)，另一个是二维码是否被扫码读取的状态。
 */
@RestController
@RequestMapping("/saas")
@Slf4j
public class SaasPassportController extends BaseInfoProperties {

    // 注入Jwt工具类
    @Autowired
    private JWTUtil jwtUtil;

    // 注入用户服务
    @Autowired
    private UsersService usersService;


    /**
     * @target 给H5端用的
     * @describe 生成二维码登录的Token给H5当做二维码使用，App用户扫码登录所获得的token就是这里生成的。
     * @return 返回生成的QR Token给前端，前端会将其转换为二维码展示给用户扫码
     */
    @PostMapping("/getQRToken")
    public GraceJSONResult getQRToken() {

        // 1️⃣生成一个全球唯一的UUID作为QR Token
        String qrToken = UUID.randomUUID().toString(); // 通过UUID生成几乎不可能重复的标识符
        log.info("这里是auth下的saas，生成的QRToken为: {}", qrToken);

        // 2️⃣把生成的QRToken(UUID)存储到Redis中，用于App端扫码登录到也获取到这个ID(记得给二维码token设置过期时间，防止二维码长时间未被使用)
        // 问：这里的值为什么要设置成qrToken呢？
        // 答：随便啦，反正记录下这个qrToken就行了，App端扫码后会和这个qrToken进行比对
        redis.set(SAAS_PLATFORM_LOGIN_TOKEN + ":" + qrToken, qrToken, 60 * 5); // 设置过期时间为5分钟，这里存到redis是因为到时候App端发过来的请求可以用来判断是否扫码登录了。

        // 3️⃣将刚生成的token在Redis中标记为未使用状态，因为网页端会每隔几秒钟轮询一次。直到手机扫码后，将Redis设置为已使用状态，这时前端界面会轮询查询后端该redis的value。
        redis.set(SAAS_PLATFORM_LOGIN_TOKEN_READ + ":" + qrToken, "0", 60 * 5);

        // 返回生成的QR Token给前端当做二维码，让下一次前端轮询时可以带上这个qrToken
        return GraceJSONResult.ok(qrToken);
    }



    /**
     * @target 给App端用的
     *
     * @return 返回预登录的新preToken给App端，App端下次请求时需要携带这个preToken
     * ----------------------------------------------
     * header：appUserId、appUserToken
     * 注意：这里的appUserId和appUserToken是手机端登录后生成的，作为登录凭证传递给后端(判断是不是已经注册的用户)。
     * query：qrToken
     * 对qrToken的理解：这个qrToken是前端生成的二维码登录的Token，App端用户扫码后会将该Token传递给后端进行验证，而后端已经在Redis中存储了该token的值。
     * @describe App端将在H5扫码到的Token传递给后端进行验证，验证通过后生成预登录的Token。
     */
    @PostMapping("/scanCode")
    public GraceJSONResult scanCode(@RequestParam String qrToken, // App端从H5所扫到的二维码登录Token
                                    @RequestHeader("appUserId") String headerAppUserId,  // 用户ID
                                    @RequestHeader("appUserToken") String headerAppUserToken,  // 用户Token
                                    HttpServletRequest request) {

        // 1️⃣对App端传来的qrToken进行判空处理
        if (StringUtils.isBlank(qrToken)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FAILED);
        }

        // 2️⃣首先判断传过来的qrToken是否在redis中存在，存在则说明二维码是有效的
        String redisQRToken = redis.get(SAAS_PLATFORM_LOGIN_TOKEN + ":" + qrToken);
        if (!redisQRToken.equalsIgnoreCase(qrToken)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FAILED);
        }

        // 3️⃣验证用户id、token是否为空、是否有效，因为gateway放行了这个请求，所以还需要判断jwt是否有效。
        if (StringUtils.isBlank(headerAppUserId) || StringUtils.isBlank(headerAppUserToken)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.HR_TICKET_INVALID);
        }
        // 下面的checkJWT可能会产生异常，所以已经在全局异常处理器中处理了JWT相关的异常
        String userJson = jwtUtil.checkJWT(headerAppUserToken.split("@")[1]);
        if (StringUtils.isBlank(userJson)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.HR_TICKET_INVALID);
        }

        // 4️⃣下面将进行正式的业务处理
        // 4.1.生成预登录的新Token，为什么要新token呢？  因为需要App端需要用户二次确认，所以需要两个token，一个是qrToken(二维码登录的Token)，另一个是预登录的Token(preToken)，这个preToken是App端扫码后生成的，用于后续的登录操作。
        String preToken = UUID.randomUUID().toString();
        // 4.2.将预登录的新Token存入Redis中(qrToken对应preToken，H5可以通过本来就有qrToken来获取这个新token)，并设置过期时间为30分钟
        redis.set(SAAS_PLATFORM_LOGIN_TOKEN + ":" + qrToken, preToken, 60 * 30);
        // 4.3.将Redis中标记为已被扫码读取，value为"1,预登录Token"，表示二维码已经被扫码读取了(这里很巧妙：前端轮询「注意轮询的时候还是会带着先前的qrToken一起过来」的时候可以顺便通过redis获得preToken去更新用户状态)
        redis.set(SAAS_PLATFORM_LOGIN_TOKEN_READ + ":" + qrToken, "1," + preToken, 60 * 30);

        // 5.返回预登录的Token给App端，App端下次请求时需要携带这个preToken
        return GraceJSONResult.ok(preToken);
    }


    /**
     * @target 给H5端用的
     * @return 返回一个数组，包含扫码状态和预登录Token(preToken)
     * @describe 前端轮询接口，前端会每隔3s就会发送请求到这个接口，这里返回一个数组[]，有可能有两个元素，也有可能只有一个元素，取决于App端什么时候扫码登录了，而且H5端可以通过本来就有qrToken来获取新的预登录Token(preToken)。
     * ⚠️注意：如果使用websocket或者netty，可以在app扫描之后，在上一个接口，直接通信浏览器（H5）进行页面扫码的状态标记
     */
    @PostMapping("/codeHasBeenRead")
    public GraceJSONResult codeHasBeenRead(String qrToken) {

        // if (StringUtils.isBlank(qrToken)) {
        //     return GraceJSONResult.errorCustom(ResponseStatusEnum.FAILED);
        // }

        // 返回一个数组，包含扫码状态和预登录Token(preToken) ，如果二维码未被扫码读取，则返回一个空数组，然后前端继续轮询这个接口
        ArrayList<Object> list = new ArrayList<>();

        // 1️⃣通过传过来qrToken，去查询Redis中该qrToken对应的状态：是1还是2
        String redisQRToken = redis.get(SAAS_PLATFORM_LOGIN_TOKEN_READ + ":" + qrToken);

        // 2️⃣判定redisQRToken是否为空
        if (StringUtils.isNotBlank(redisQRToken)) {

            // 将redisQRToken拆分成数组
            String[] parts = redisQRToken.split(",");

            // 3️⃣如果redisQRToken的长度大于1，说明二维码已经被扫码读取了
            if (parts.length > 1) {
                // 4️⃣将预登录的Token添加到list中
                list.add(Integer.valueOf(parts[0])); // 这里的parts[0]是"1"或"0"，表示二维码是否被扫码读取
                list.add(parts[1]); // parts[1]是预登录的Token,前端也需要这个新的Token来进行后续的登录操作
            } else {
                // 5️⃣如果长度不大于1，说明二维码未被扫码读取，返回空数组
                list.add("");
            }
        }

        // 返回一个包含扫码状态和预登录Token的数组
        return GraceJSONResult.ok(list); // SpringMVC会将该数组转成JSON字符串返回给前端
    }



    /**
     * @target 给App端确认登录时使用
     *
     * @param userId   用户ID
     * @param qrToken  二维码登录的Token
     * @param preToken 预登录的Token
     * @return 返回登录结果
     *
     * @describe App端用户确认登录后，我就将用户信息暂存到Redis中，方便H5端获取用户信息(注意App端并不需要做些什么)
     * 注：如果使用websocket或者netty，可以在此直接通信H5进行页面的跳转，不用要H5端再去请求这个接口了。
     */
    @PostMapping("/goQRLogin")
    public GraceJSONResult goQRLogin(String userId, String qrToken, String preToken) {

        // 问：为什么App端要传qrToken和preToken呢，直接传userId不就行了嘛？
        // 答：假如有人疯狂请求这个接口，只需传入一个userId，那么我的redis就会老被覆盖，导致H5端无法获取到用户信息(因为这个key一直在进行set操作，H5端get不到redis)

        // 1️⃣App端携带qrToken和preToken来到后端，首先通过qrToken获取到先前存着的预登录Token(preToken)
        String preTokenRedisArray = redis.get(SAAS_PLATFORM_LOGIN_TOKEN_READ + ":" + qrToken);

        // 2️⃣如果获取到的preTokenRedisArray不为空，说明二维码已经被扫码读取
        if (StringUtils.isNotBlank(preTokenRedisArray)) {

            // 从redis中获取预登录的新Token
            String preTokenRedis = preTokenRedisArray.split(",")[1];

            // 判断App端传入的preToken是否与redis中的预登录Token一致，如果真的一致，就将用户信息存入Redis中
            if (preTokenRedis.equalsIgnoreCase(preToken)) {

                // 根据用户id，从mapper中查询用户信息
                Users hrUsers = usersService.getById(userId);
                // 判空处理
                if (hrUsers == null) {
                    return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
                }

                // 3️⃣将用户存入Redis中，因为H5在未登录的情况下，拿不到用户id，所以暂存用户信息到Redis，到时候H5可以通过redis直接获取到相关的用户信息
                // 如果使用WebSocket或Netty，可以直接通信H5获得用户id，则无此问题
                redis.set(REDIS_SAAS_USER_INFO + ":temp:" + preToken, new Gson().toJson(hrUsers), 5 * 60); // 设置过期时间为5分钟
            }
        }

        return GraceJSONResult.ok();
    }



    /**
     * @target 供Sass端用的
     * @param preToken 前端传过来的预登录的preToken
     * @describe 给H5端返回一个JWT Token并存到cookie中，后续通过这个Token可以获取到用户信息。
     */
    @PostMapping("/checkLogin")
    public GraceJSONResult checkLogin(String preToken) {

        // 1️⃣判空处理
        if (StringUtils.isBlank(preToken)) {
            return GraceJSONResult.ok("");
        }

        // 2️⃣通过Redis获取App端‘确认登录’所在redis下存的用户信息，用来生成唯一的jwtToken
        String userJson = redis.get(REDIS_SAAS_USER_INFO + ":temp:" + preToken); // 有可能过期了，需要判空
        if (StringUtils.isBlank(userJson)) { // 如果用户信息不存在，说明用户未登录或者预登录Token已过期
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        // 3️⃣生成一个JWT Token，并且长期有效
        String saasUserToken = jwtUtil.createJWTWithPrefix(userJson, TOKEN_SAAS_PREFIX);

        // 4️⃣将用户信息存入Redis中，长期有效(这里将放redis是因为H5端只能需要通过jwt Token获取用户信息)
        redis.set(REDIS_SAAS_USER_INFO + ":" + saasUserToken, userJson);

        // 5️⃣返回生成的SaaS用户的JWT Token给前端，前端可以使用这个JWT Token进行后续的操作
        return GraceJSONResult.ok(saasUserToken);
    }


    /**
     * @describe H5端传来一个jwt token，根据这个token获取相关的用户信息，比如用户的：username、name、face。
     */
    @GetMapping("info")
    public GraceJSONResult info(String token) {

        // 通过前端传来的jwtToken，在redis中获取用户信息
        String userJson = redis.get(REDIS_SAAS_USER_INFO + ":" + token);
        // 判空处理
        if (StringUtils.isBlank(userJson)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        // 将redis里面json字符串转换为Users对象
        Users SaasUser = new Gson().fromJson(userJson, Users.class);
        // 直接传整个users可太大了，因为会占用很多的带宽，所以这里使用VO对象来传输数据
        SaasUserVO saasUserVO = new SaasUserVO();
        // 属性拷贝
        BeanUtils.copyProperties(SaasUser, saasUserVO);

        // 返回用户信息给H5
        return GraceJSONResult.ok(saasUserVO);
    }


    /**
     * @describe H5端传来一个token，进行登出操作(后端不做操作，只是前端把token删除了)
     */
    @PostMapping("logout")
    public GraceJSONResult logout(String token) {
        return GraceJSONResult.ok();
    }
}

