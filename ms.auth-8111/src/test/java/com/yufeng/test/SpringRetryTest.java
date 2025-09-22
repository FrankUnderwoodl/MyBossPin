package com.yufeng.test;

import com.yufeng.api.retry.RetryComponent;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Lzm
 * @CreateTime 2025年6月17日 12:30
 *
 */
@SpringBootTest
@Slf4j
public class SpringRetryTest {

    // 注入组件
    @Autowired
    private RetryComponent retryComponent;

    @Test
    public void retry() {
        // 调用重试方法
        boolean res = retryComponent.sendSmsWithRetry();
        // 打印结果-
        log.info("最终sendSmsWithRetry的运行结果为 res = {}", res);
    }
}
