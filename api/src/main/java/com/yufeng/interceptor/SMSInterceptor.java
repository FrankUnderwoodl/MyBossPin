package com.yufeng.interceptor;

import com.yufeng.base.BaseInfoProperties;
import com.yufeng.exception.GraceException;
import com.yufeng.exception.MyCustomException;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Lzm
 * @CreateTime 2025年6月16日 05:55
 */
@Slf4j
public class SMSInterceptor extends BaseInfoProperties implements HandlerInterceptor {

    // 对‘/passport/getSMSCode’这个路径进行intercept，判断IP是否在60秒内重复获取验证码
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 获取ip
        String userIp = IPUtil.getRequestIp(request).replace(":", ".");

        // 通过查询Redis，查看该IP是否有获取验证码的记录，如果有，则说明在60秒内重复获取验证码(拒绝放行)
        boolean keyIsExist = redis.keyIsExist(MOBILE_SMSCODE + ":" + userIp);
        if (keyIsExist) {
            log.error("IP: {} 重复在60秒内重复获取验证码！！！", userIp);

            // 直接抛出异常，这样对代码的侵入性大、而且也不能return false，一般不推荐(解决方案就是再封装一个异常类: GraceException)
            // throw new MyCustomException(ResponseStatusEnum.SMS_NEED_WAIT_ERROR);
            GraceException.doException(ResponseStatusEnum.SMS_NEED_WAIT_ERROR);
            return false; // 这里return false是为了阻止请求继续处理
        }
        return true;
    }
}
