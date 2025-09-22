
//	此资源由 58学课资源站 收集整理
//	想要获取完整课件资料 请访问：58xueke.com
//	百万资源 畅享学习
package com.yufeng.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

/**
 * @author Lzm
 * @describe 模拟发送短信的工具类
 */
// @Component  // 因为是静态方法会存到堆空间中，所以不需要被Spring管理
@Slf4j
// @Component
public class SMSUtilsRetry {

    /*
      假设并且模拟本方法为发送短信，如果返回true表示正常，否则不正常，异常则重试
     */
    public static boolean sendSMS() throws RuntimeException {

        int num = RandomUtils.nextInt(0, 8);
        log.info("生成的随机数为 num = {}", num);

        switch (num) {
            case 0: {
                // 模拟发送异常
                throw new IllegalArgumentException("参数有误，不能为0");
            }
            case 1: {
                // 1为正常返回数据
                throw new IllegalArgumentException("参数有误，不能为1");
                // return true;
            }
            case 2: {
                // 模拟数组异常
                throw new ArrayIndexOutOfBoundsException("数据越界...");
            }
            case 3: {
                // 调用正常但是第三方返回的参数不对，针对false则需要自行处理
                // 第三方sdk(比如短信、支付)一般都会返回不同的状态码，对照状态码列表可以自行做额外的处理
                // return false;
                throw new ArrayIndexOutOfBoundsException("数据越界3...");
            }
        }

        // 其他数，则触发最终的别的异常
        throw new NullPointerException("空指针，其他数值异常");
    }

}


