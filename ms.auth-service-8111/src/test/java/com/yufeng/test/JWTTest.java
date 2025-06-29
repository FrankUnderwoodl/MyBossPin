package com.yufeng.test;

import com.google.gson.Gson;
import com.yufeng.pojo.Stu;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import sun.misc.BASE64Encoder;

import javax.crypto.SecretKey;

/**
 * @author Lzm
 * @CreateTime 2025年6月17日 12:30
 */
@SpringBootTest
public class JWTTest {

    // 定义秘钥，提供给jwt加密解密，一般都是公司定的规范
    private static final String USER_KEY = "mybosspin_1234567890abcdefg";

    @Test
    public void createJWT() {

        // 1.首先秘钥进行base64编码
        String base64 = new BASE64Encoder().encode(USER_KEY.getBytes());

        // 2.对base64生成一个秘钥对象
        SecretKey secretKey = Keys.hmacShaKeyFor(base64.getBytes());

        // 3. 通过jwt去生成一个token字符串
        Stu stu = new Stu(1001, "kzx", 18);
        String stuJson = new Gson().toJson(stu); // 将对象转换为JSON字符串

        // 3.通过jwt去生成token
        String myJWT = Jwts.builder()
                .setSubject(stuJson) // 设置主题(也就是设置用户信息)
                .signWith(secretKey) // 使用秘钥进行签名(这里会自动生成header，也就是{"alg": "HS256",    // 因为你用的是HMAC SHA-256算法 "typ": "JWT" // 固定值，表示这是JWT})
                .compact(); // 生成token
        System.out.println("生成的JWT为: " + myJWT);
    }


    @Test
    public void checkJWT() {

        // 模拟从前端获取到的token
        String myJWT = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ7XCJpZFwiOjEwMDEsXCJuYW1lXCI6XCJrenhcIixcImFnZVwiOjE4fSJ9.shjmun-ni-UvAvIL4fOYSFZnbl-s6-OIdf3aENH7fjU";
        // 1.首先秘钥进行base64编码
        String base64 = new BASE64Encoder().encode(USER_KEY.getBytes());
        // 2.对base64生成一个秘钥对象(这里底层会检测到你使用了HMAC SHA-256算法，自动设置了header)
        SecretKey secretKey = Keys.hmacShaKeyFor(base64.getBytes());
        // 3.通过jwt去解析token
        String subject = Jwts.parserBuilder()
                .setSigningKey(secretKey) // 设置签名的秘钥
                .build() // 构建解析器
                .parseClaimsJws(myJWT) // 解析token(如果此处抛出异常，则说明token无效或者被篡改了)
                .getBody() // 获取主体部分
                .getSubject(); // 获取主题信息(这只是主体的一部分，还有其他信息，比如过期时间等)

        System.out.println("解析出来的JWT的主题信息为: " + subject);
    }
}
