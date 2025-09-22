
//	此资源由 58学课资源站 收集整理
//	想要获取完整课件资料 请访问：58xueke.com
//	百万资源 畅享学习
package com.yufeng.utils;

import org.springframework.util.DigestUtils;

/**
 * MD5 加密工具类
 */
public class MD5Utils {

    /**
     * MD5混合加密
     * @param data: 待加密字符串
     * @param slat: 盐，用于混合md5加密
     * @return
     */
    public static String encrypt(String data, String slat) {
        String base = data + slat; // 将密码和盐进行拼接
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes()); // 通过Spring的DigestUtils进行MD5加密
        return md5;
    }

    /**
     * 生成随机盐值
     * @return 返回一个随机的盐值
     */
    public static String getRandomSalt() {

        // 生成一个6位的随机数字字符串
        return (int)((Math.random() * 9 + 1) * 100000) + "";
    }

    public static void main(String[] args) {
       String md5Str = MD5Utils.encrypt("123456", "8111");
        System.out.println(md5Str);
    }
}
