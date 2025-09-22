package com.yufeng.api.retry;

import com.yufeng.utils.SMSUtilsRetry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Lzm
 * @CreateTime 2025年7月15日 23:55
 */
@Slf4j
@Component
public class RetryComponent {

    /**
     * Retryable参数详解：
     * - value：指定需要重试的异常类型，如果抛出的异常不在这个列表中，则不会进行重试。
     * - maxAttempts：指定重试的总次数，包括第一次调用，所以如果设置为5，则实际会尝试5次。
     * - backoff：指定重试的间隔时间和倍数。这里的delay是初始延迟时间，multiplier是每次重试的倍数，比如第一次是1s，第二次是2s，第三次是4s，以此类推。
     */
    @Retryable(value = {IllegalArgumentException.class, ArrayIndexOutOfBoundsException.class, Exception.class},
            maxAttempts = 5, backoff = @Backoff(delay = 1000L, multiplier = 2))
    public boolean sendSmsWithRetry() {

        log.info("执行了一次sendSMS操作，当前时间 Time={}", LocalDateTime.now());
        // 这里可以调用SMSUtilsRetry.sendSMS()方法进行短信发送
        return SMSUtilsRetry.sendSMS();
    }

    // 如果达到最大重试次数，或者抛出一个没有被设置（进行重试）的异常，可以作为方法的最终兜底处理
    @Recover
    public boolean recover() {
        // 这里可以进行一些日志记录或者其他处理
        log.error("这里是兜底方法，短信发送失败，已达到最大重试次数");
        return false; // 返回一个默认值或者抛出异常
    }
}
