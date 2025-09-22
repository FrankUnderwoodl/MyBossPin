package com.yufeng.api.bean_config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.yufeng.exception.GraceException;
import com.yufeng.grace.result.ResponseStatusEnum;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Lzm
 * @CreateTime 2025年8月26日 11:26
 */
@Configuration
public class SentinelBlockConfig implements BlockExceptionHandler {

    // 全局的限流处理器
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws Exception {
        GraceException.doException(ResponseStatusEnum.SENTINEL_BLOCK_FLOW_LIMIT_ERROR);
    }
}
