package com.yufeng.utils;

import com.yufeng.exception.GraceException;
import com.yufeng.grace.result.ResponseStatusEnum;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * @author Lzm
 * @CreateTime 2025年6月17日 22:47
 */
@Component
@Slf4j
@RefreshScope // 这个注解可以让配置中心的配置变更后，自动刷新到当前类中
@Lazy  // 延迟到真正使用时才创建，也就是他妈的在Spring初始化的时候，并不会注入到IOC容器中
public class JWTUtil {

    // JWT令牌的前缀，通常用于区分不同的应用或服务
    public static final String AT = "@";

    // 签名的秘钥属性类
    @Autowired
    private JWTProperties jwtProperties;

    // 通过nacos配置中心来获取JWT的秘钥
    // @Value("${jwt.key}")
    @Value("myVeryLongSecretKeyThatIsAtLeast32Characters123456780")
    private String JWT_KEY;

    // 通过配置文件获取JWT秘钥
    // private String JWT_KEY = jwtProperties.getKey();

    @Test
    public void testJWTKey() {
        // 测试获取JWT秘钥
        // log.info("从配置中心获取的JWT秘钥为: {}", JWT_KEY);
        // String JWT_KEY = jwtProperties.getKey();
        // log.info("从配置文件获取的JWT秘钥为: {}", JWT_KEY);
    }

    /**
     * 因为设计到多端，所以这里提供一个方法，可以在生成JWT令牌时添加一个前缀
     */
    public String createJWTWithPrefix(String body, String prefix) {
        // 返还带有前缀的JWT令牌
        return prefix + AT + this.createJWT(body);
    }


    public String createJWTWithPrefix(String body, String prefix, Long expireTime) {

        // 首先判断传过来的‘过期时间’是否为null或者小于等于0
        if (expireTime == null || expireTime <= 0) {
            GraceException.doException(ResponseStatusEnum.SYSTEM_NO_EXPIRE_ERROR);
        }
        // 返还带有前缀的JWT令牌
        return prefix + AT + this.createJWT(body, expireTime);
    }


    /**
     * 生成JWT令牌的方法(不设置过期时间)
     * 问: 这里为什么再进行封装呢？
     * 答: 因为这样调用的时候，更加简洁明了、舒服😌
     */
    public String createJWT(String body) {
        // 直接调用dealJWT方法，传入主体内容(通常是用户信息)，不设置过期时间
        return this.dealJWT(body, null);
    }


    /**
     * 生成JWT令牌的方法(设置过期时间)
     * 问: 这里为什么再进行封装呢？
     * 答: 因为这样调用的时候，更加简洁明了、舒服??
     */
    public String createJWT(String body, Long expireTime) {
        // 直接调用dealJWT方法，传入主体内容(通常是用户信息)，设置过期时间
        // 这里可以判断expireTime是否为null，如果为null，则就抛出异常
        if (expireTime == null || expireTime <= 0) {
            GraceException.doException(ResponseStatusEnum.SYSTEM_NO_EXPIRE_ERROR);
        }
        return this.dealJWT(body, expireTime);
    }


    /**
     * 生成JWT令牌的前置处理方法(把生成另外一个秘钥的过程封装起来，这样做可以解耦和简化代码)
     * 这里的JWT令牌是一个字符串，包含了用户信息等内容
     */
    public String dealJWT(String body, Long expireTime) {

        // 1.首先秘钥进行base64编码
        String base64 = new BASE64Encoder().encode(JWT_KEY.getBytes());
        // String base64 = new BASE64Encoder().encode("myVeryLongSecretKeyThatIsAtLeast32Characters123456780".getBytes());
        // String base64 = JWT_KEY; // 这里直接使用从配置文件中获取的秘钥
        // log.info("这里是通过nacos过来的！JWT秘钥为: {}", base64);

        // 2.对base64生成一个秘钥对象，也就是通过HMAC SHA-256算法生成一个秘钥对象
        SecretKey secretKey = Keys.hmacShaKeyFor(base64.getBytes());

        // 3. 调用generateToken方法生成JWT令牌(记得先判断传来的expireTime参数是否为null)
        String jwtToken;
        if (expireTime == null) {
            jwtToken = generateToken(body, secretKey); // 如果不设置过期时间，则直接生成JWT令牌
        } else {
            jwtToken = generateTokenExpire(body, expireTime, secretKey);
        }

        // 4.返回生成的JWT令牌
        return jwtToken;
    }


    /**
     * 生成JWT令牌(不设置过期时间)
     *
     * @param body      主题内容(通常是用户信息)
     * @param secretKey 秘钥
     * @return          生成的JWT令牌
     */
    public String generateToken(String body, SecretKey secretKey) {
        return Jwts.builder()
                .setSubject(body) // 设置主题(也就是设置用户信息)
                .signWith(secretKey) // 使用秘钥进行签名
                .compact();
    }


    /**
     * 生成JWT令牌(设置过期时间)
     *
     * @param body        主题内容(通常是用户信息)
     * @param expireTime  过期时间(单位为毫秒)
     * @param secretKey   秘钥
     * @return            生成的JWT令牌
     */
    public String generateTokenExpire(String body, Long expireTime, SecretKey secretKey) {
        // 定义过期时间
        Date date = new Date(System.currentTimeMillis() + expireTime);

        // 4.通过jwt去生成token
        return Jwts.builder()
                .setSubject(body) // 设置主题(其实就是设置用户信息)
                .setExpiration(date) // 设置过期时间
                .signWith(secretKey) // 使用秘钥进行签名
                .compact();
    }



    /**
     * 校验JWT令牌的方法
     * @param pendingJWT 待校验的JWT令牌
     * @return 返回解析后的主题信息(通常是用户信息)
     */
    public String checkJWT(String pendingJWT) {

        // 0.检测一下密钥是否从nacos配置中拿到
        // log.info("这里是checkJWT，正在解析对象，JWT秘钥为: {}", JWT_KEY);

        // 1.首先对秘钥进行base64编码
        String base64 = new BASE64Encoder().encode(JWT_KEY.getBytes());
        // String base64 = new BASE64Encoder().encode("myVeryLongSecretKeyThatIsAtLeast32Characters123456780".getBytes());
        // 2.对base64生成一个秘钥对象(这里底层会检测到你使用了HMAC SHA-256算法，自动设置了header)
        SecretKey secretKey = Keys.hmacShaKeyFor(base64.getBytes());

        /* 这里会抛出异常：
        签名验证失败 - 抛出 SignatureException
        令牌格式错误 - 抛出 MalformedJwtException
        令牌已过期 - 抛出 ExpiredJwtException
        令牌尚未生效 - 抛出 PrematureJwtException
        其他JWT相关错误 - 抛出 JwtException */
        // 3.通过jwt去解析token
        String subject = Jwts.parserBuilder()
                .setSigningKey(secretKey) // 设置签名的秘钥
                .build() // 构建解析器
                .parseClaimsJws(pendingJWT) // 解析token(如果此处抛出异常，则说明token无效或者被篡改了)
                .getBody() // 获取主体部分
                .getSubject(); // 获取主题信息(这只是主体的一部分，还有其他信息，比如过期时间等)

        // System.out.println("这里是checkJWT，正在解析对象，解析出来的JWT的对象信息为: " + subject);
        // log.info("这里是checkJWT，正在解析对象，解析出来的JWT的对象信息为: {}", subject);
        return subject;
    }

    /**
     * 测试JWT生成和解析
     */
    @Test
    public void testJWT() {
        // 测试生成JWT令牌
        String body = "{\"id\":\"12345\",\"name\":\"testUser\"}";
        String jwt = this.createJWT(body);
        log.info("生成的JWT令牌为: {}", jwt);

        // 测试解析JWT令牌
        String parsedBody = this.checkJWT(jwt);
        log.info("解析后的JWT令牌内容为: {}", parsedBody);
    }
}
