package com.yufeng.test;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentPBEConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Lzm
 * @CreateTime 2025年7月18日 03:43
 * @describe @SpringBootTest注解用于加载 Spring Boot 的应用上下文，
 * 是的，使用 @SpringBootTest 时会启动整个 Spring Boot 应用程序，包括：
 * • 加载所有 @Component、@Service、@Repository 等 Bean
 * • 启动嵌入式服务器（如 Tomcat）
 * • 执行自动配置
 * • 连接数据库等外部资源
 */
@SpringBootTest
public class JasyptTest {

    @Test
    public void testPwdEncrypt() {

        //· 1.实例化加密器(专门对 String 类型进行加密的)
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

        //· 2.配置加密算法和密钥
        EnvironmentPBEConfig config = new EnvironmentPBEConfig();
        config.setPassword("Lzm"); // 用于加密的秘钥(盐)，可以是随机字符串，或者固定，一定要存储好，不要被其他人知道(一般是技术经理或者架构师才能知道这个密钥)
        config.setAlgorithm("PBEWithMD5AndDES"); // 设置加密算法，默认就是 PBEWithMD5AndDES
        encryptor.setConfig(config); // 将config放进加密器中

        //· 3.使用加密器进行加密
        String myPwd = "33321480";
        String encryptedPwd = encryptor.encrypt(myPwd);

        System.out.println("++++++++++++++++++++++++++++++");
        System.out.println("+ 原密码为：" + myPwd);
        System.out.println("+ 加密后的密码为：" + encryptedPwd);
        System.out.println("++++++++++++++++++++++++++++++");

        // 厉害呀，每次加密到的密文都不一样
        // sBKFZZ0IINZXoEVLf1rnNw2vfBUxjcon
        // gbVcyVXncfqM8dRR6mpio+5JYji57rdQ
    }




    @Test
    public void testPwdDecrypt() {

        //· 1.实例化加密器(专门对 String 类型进行加密的)
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

        //· 2.配置加密算法和密钥
        EnvironmentPBEConfig config = new EnvironmentPBEConfig();
        config.setPassword("Lzm"); // 用于加密的秘钥(盐)，可以是随机字符串，或者固定，一定要存储好，不要被其他人知道(一般是技术经理或者架构师才能知道这个密钥)
        config.setAlgorithm("PBEWithMD5AndDES"); // 设置加密算法，默认就是 PBEWithMD5AndDES
        encryptor.setConfig(config); // 将config放进加密器中

        //· 3.使用加密器进行加密
        String pendingPwd = "gbVcyVXncfqM8dRR6mpio+5JYji57rdQ";
        String decryptedPwd = encryptor.decrypt(pendingPwd);

        System.out.println("++++++++++++++++++++++++++++++");
        System.out.println("+ 原密码为：" + pendingPwd);
        System.out.println("+ 解密后的密码为：" + decryptedPwd);
        System.out.println("++++++++++++++++++++++++++++++");

    }



}
